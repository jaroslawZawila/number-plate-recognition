package net.zawila.numberplate

import cats.effect.IO
import cats.implicits._
import net.zawila.numberplate.model.S3Location
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}
import software.amazon.awssdk.services.rekognition.RekognitionClient
import software.amazon.awssdk.services.rekognition.model.{DetectTextRequest, DetectTextResponse, TextDetection}

class ImageProcessorTest extends WordSpec with MockitoSugar with MustMatchers {
  val aws = mock[RekognitionClient]
  val proc = new ImageProcessor[IO](aws).detectText
  val s3Location = S3Location("bucket", "key")

  "Image Process " must {
    "work" in {
      val awsResponse = DetectTextResponse.builder().textDetections(TextDetection.builder().detectedText("Hey").build()).build()
      when(aws.detectText(any[DetectTextRequest]())).thenReturn(awsResponse)

      val response = fs2.Stream.emit(s3Location).covary[IO].through(proc).compile.foldMonoid.unsafeRunSync()

      response mustBe "Hey"
    }
  }

}
