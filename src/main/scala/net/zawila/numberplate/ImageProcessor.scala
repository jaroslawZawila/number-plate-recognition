package net.zawila.numberplate

import cats.effect.Effect
import fs2.Stream
import net.zawila.numberplate.model.S3Location
import software.amazon.awssdk.services.rekognition.RekognitionClient
import software.amazon.awssdk.services.rekognition.model.{DetectTextRequest, Image, S3Object}

import scala.collection.JavaConverters._

class ImageProcessor[F[_]] (rekognito: RekognitionClient = RekognitionClient.builder().build()) {

  def detectText(implicit F: Effect[F]): fs2.Pipe[F, S3Location, String] =
    _.flatMap( x =>
        getMeterRead(x.bucket, x.key)
    )


  def getMeterRead(bucket: String, key: String)(implicit F: Effect[F]): Stream[F, String] = {
    val request = DetectTextRequest.builder()
    val image = Image.builder()
    val s3Object = S3Object.builder()
    s3Object.bucket(bucket).name(key)

    request.image(image.s3Object(s3Object.build()).build())
    val response = F.delay(rekognito.detectText(request.build))

    fs2.Stream
      .eval(response)
      .map(_.textDetections().asScala.head)
      .map(_.detectedText())
  }
}
