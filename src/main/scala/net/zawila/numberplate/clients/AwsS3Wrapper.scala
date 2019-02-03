package net.zawila.numberplate.clients

import cats.effect.Effect
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model._

class AwsS3Wrapper[F[_]] {

  private val s3Client: S3Client = S3Client.builder().region(Region.EU_WEST_1).build()

  def completeMultipartUpload(request: CompleteMultipartUploadRequest)(implicit F: Effect[F]): F[CompleteMultipartUploadResponse] = F.delay {
    s3Client.completeMultipartUpload(request)
  }

  def createMultipartUpload(request: CreateMultipartUploadRequest)(implicit F: Effect[F]): F[CreateMultipartUploadResponse] = F.delay {
    s3Client.createMultipartUpload(request)
  }

  def uploadPart(request: UploadPartRequest, body: RequestBody)(implicit F: Effect[F]) = F.delay{
    s3Client.uploadPart(request,body).eTag()
  }
}
