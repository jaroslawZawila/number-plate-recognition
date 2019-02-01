package net.zawila.numberplate

import cats.effect.{Effect, IO}
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext

object HelloWorldServer extends fs2.StreamApp[IO] {
  import scala.concurrent.ExecutionContext.Implicits.global

  def stream(args: List[String], requestShutdown: IO[Unit]) = ServerStream.stream[IO]
}

object ServerStream {

  def healthcheckService[F[_]: Effect] = new HealtchCheck[F].service
  def numberPlateService[F[_]: Effect] = new NumberPlateService[F].service

  def stream[F[_]: Effect](implicit ec: ExecutionContext) =
    BlazeBuilder[F]
      .bindHttp(8080, "0.0.0.0")
      .mountService(healthcheckService, "/")
      .mountService(numberPlateService, "/number-plates")
      .serve
}
