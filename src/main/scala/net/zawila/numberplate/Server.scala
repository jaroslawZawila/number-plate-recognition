package net.zawila.numberplate

import cats.effect.{Effect, IO}
import kamon.Kamon
import kamon.http4s.middleware.server.KamonSupport
import kamon.zipkin.ZipkinReporter
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext

object HelloWorldServer extends fs2.StreamApp[IO] {
  import scala.concurrent.ExecutionContext.Implicits.global

  def stream(args: List[String], requestShutdown: IO[Unit]) = ServerStream.stream[IO]
}

object ServerStream {

  def healthcheckService[F[_]: Effect] = new HealtchCheck[F].service
  def numberPlateService[F[_]: Effect] = new NumberPlateService[F].service

  Kamon.addReporter(new ZipkinReporter())

  def stream[F[_]: Effect](implicit ec: ExecutionContext) =
    BlazeBuilder[F]
      .bindHttp(8080, "0.0.0.0")
      .mountService(KamonSupport.apply(healthcheckService), "/")
      .mountService(numberPlateService, "/number-plates")
      .serve
}