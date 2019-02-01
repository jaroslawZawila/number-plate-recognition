package net.zawila.numberplate

import cats.effect.Effect
import io.circe.Json
import org.http4s.HttpService
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

class HealtchCheck[F[_]: Effect] extends Http4sDsl[F] {

  val service: HttpService[F] = {
    HttpService[F] {
      case GET -> Root / "ping" =>
        Ok(Json.obj("message" -> Json.fromString(s"pong")))
    }
  }
}
