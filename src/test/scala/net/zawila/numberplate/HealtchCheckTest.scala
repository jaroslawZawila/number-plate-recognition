package net.zawila.numberplate

import cats.data.OptionT
import cats.effect.IO
import org.http4s.{Request, Response, Uri}
import org.scalatest._

class HealtchCheckTest extends WordSpec with MustMatchers {

  val service = new HealtchCheck[IO].service

  "First test" must {
    "pass" in {
      val req = Request[IO](uri = Uri.uri("/ping"))

      val resp: OptionT[IO, Response[IO]] =  service.run(req)

      resp.value.unsafeRunSync().map{r =>
        r.status.code mustBe 200
        r.as[String].unsafeRunSync() mustBe "{\"message\":\"pong\"}"
      }
    }
  }

}
