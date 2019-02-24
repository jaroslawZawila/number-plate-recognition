val Http4sVersion = "0.18.21"
val Specs2Version = "4.1.0"
val LogbackVersion = "1.2.3"

lazy val root = (project in file("."))
  .settings(
    organization := "net.zawila",
    name := "meter-read",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.7",
    libraryDependencies ++= Seq(
      "org.http4s"      %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"      %% "http4s-circe"        % Http4sVersion,
      "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
      "org.scalatest" %% "scalatest" % "3.0.5" % Test,
      "org.mockito" % "mockito-core" % "2.23.4" % Test,
      "ch.qos.logback"  %  "logback-classic"     % LogbackVersion,
      "software.amazon.awssdk" % "aws-sdk-java" % "2.3.9",
      "io.kamon" %% "kamon-core" % "1.1.2",
      "io.kamon" %% "kamon-http4s" % "1.0.7",
      "io.kamon" %% "kamon-zipkin" % "1.0.0"
    ),
    
    addCompilerPlugin("org.spire-math" %% "kind-projector"     % "0.9.6"),
    addCompilerPlugin("com.olegpy"     %% "better-monadic-for" % "0.2.4")
  )

