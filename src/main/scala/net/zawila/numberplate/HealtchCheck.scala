package net.zawila.numberplate

import cats.effect.Effect
import io.circe.Json
import kamon.Kamon
import org.http4s.HttpService
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

import scala.util.Random

class HealtchCheck[F[_]: Effect] extends Http4sDsl[F] {

  val service: HttpService[F] = {
    HttpService[F] {
      case GET -> Root / "ping" => {
        val s =Kamon.buildSpan("ping").start()
        Thread.sleep(Random.nextInt(500))
        s.finish()
        Ok(Json.obj("message" -> Json.fromString(s"pong")))
      }

    }
  }
}
