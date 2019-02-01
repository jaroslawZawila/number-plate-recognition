package net.zawila.numberplate

import java.io.ByteArrayInputStream

import cats.effect.Effect
import fs2.Chunk
import org.http4s.multipart.Part
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model._

import scala.collection.JavaConverters._

class S3Uploader [F[_]] {

  private val s3Client: S3Client = S3Client.builder().region(Region.EU_WEST_1).build()

  def uploadFileRaw(bucket: String, fileName: String)(implicit F: Effect[F]): fs2.Pipe[F, Part[F], S3Location] =
    _.flatMap(
      _.body.through(uploadFile(bucket, fileName)))

  private def uploadFile(bucket: String, key: String)(implicit F: Effect[F]): fs2.Pipe[F, Byte, S3Location] = {

    def c(uploadId: String): fs2.Pipe[F, List[CompletedPart], S3Location] =
      _.flatMap(
        parts => {
          println(s"Parts: $parts")
          val completedMultipartUpload = CompletedMultipartUpload.builder.parts(parts.reverse.asJava).build
          val  completeMultipartUploadRequest =
            CompleteMultipartUploadRequest.builder().bucket(bucket).key(key).uploadId(uploadId)
              .multipartUpload(completedMultipartUpload).build()

          fs2.Stream.eval(complete(completeMultipartUploadRequest)).map(_ => S3Location(bucket, key))
        }

      )

    def complete(request: CompleteMultipartUploadRequest) = F.delay {
      val response: CompleteMultipartUploadResponse = s3Client.completeMultipartUpload(request)
      response
    }

    def uploadPart(uploadId: String): fs2.Pipe[F, (Chunk[Byte], Long), CompletedPart] =
      _.flatMap({
        case (chunk, index) =>
          fs2.Stream.eval(
            uploadParts(uploadId, bucket, key, index.toInt, chunk))
            .map{r =>CompletedPart.builder.partNumber(index.toInt).eTag(r).build}
      })

    def uploadParts(uploadId: String, bucket: String, key: String, partNumber: Int = 1, chunk: Chunk[Byte])(implicit F: Effect[F]) = F.delay {
      val uploadPartRequest = UploadPartRequest.builder().bucket(bucket).key(key)
        .uploadId(uploadId)
        .partNumber(partNumber)
        .contentLength(chunk.size.toLong)
        .build()

      println(s"Part number: $partNumber chunk size: ${chunk.size}")

      s3Client.uploadPart(uploadPartRequest, RequestBody.fromInputStream(new ByteArrayInputStream(chunk.toArray), chunk.size.toLong)).eTag()
    }

    def initUpload = F.delay {
      val createMultipartUploadRequest: CreateMultipartUploadRequest = CreateMultipartUploadRequest.builder()
        .bucket(bucket).key(key)
        .build()

      val response = s3Client.createMultipartUpload(createMultipartUploadRequest)
      response.uploadId()
    }

    in =>
      fs2.Stream
        .eval(initUpload)
        .flatMap(uploadId => {
          in.chunks
            .fold(Chunk.empty[Byte]){(l, c) => {
              val x = l.toList
              val y = c.toList
              val z: Seq[Byte] = x ++ y
              Chunk.apply(z :_*)
            }}
            .zip(fs2.Stream.iterate(1L)(_ + 1))
            .through(uploadPart(uploadId))
            .fold(List.empty[CompletedPart]){(l, p) => p :: l}
            .through(c(uploadId))
        })
  }
}