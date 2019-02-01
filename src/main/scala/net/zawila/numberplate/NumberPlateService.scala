package net.zawila.numberplate

import java.util.UUID

import cats.effect.Effect
import cats.implicits._
import org.http4s.HttpService
import org.http4s.dsl.Http4sDsl
import org.http4s.multipart.{Multipart, Part}


class NumberPlateService [F[_]: Effect] extends Http4sDsl[F]  {

  private val bucket = "jarek-test"
  private def filename = UUID.randomUUID().toString

  val service: HttpService[F] = {
    HttpService[F] {
      case req @ POST -> Root / "ping" => {
        req.decode[Multipart[F]] { m =>

          val  p = fs2.Stream.emits(m.parts)
            .filter(_.filename.fold(false)(_.contains(".jpg")))
            .covary[F]
            .through(uploadFileRaw(bucket, filename))
            .through(detectText)
            .covary
            .compile.foldMonoid

          Ok(p)
        }
      }
    }
  }

  private def uploadFileRaw(bucket: String, fileName: String): fs2.Pipe[F, Part[F], S3Location]= new S3Uploader[F].uploadFileRaw(bucket, fileName)
  private def detectText: fs2.Pipe[F, S3Location, String] = new ImageProcessor[F].detectText
}
