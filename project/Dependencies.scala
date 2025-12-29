import sbt._

object Version {
  val scala = "2.12.10"
  val http4s = "0.21.0-M5"
  val paradise = "2.1.1"
}

object CompilerPlugins {
  lazy val paradise = "org.scalamacros" % "paradise" % Version.paradise cross CrossVersion.full
}

object Dependencies {
  lazy val nasa4s = Seq(
    "org.http4s" %% "http4s-dsl" % Version.http4s,
    "org.http4s" %% "http4s-blaze-client" % Version.http4s,
    "com.typesafe" % "config" % "1.4.0",
    "org.typelevel" %% "cats-effect" % "2.0.0",
    "io.circe" %% "circe-core" % "0.12.3",
    "io.circe" %% "circe-generic" % "0.12.3",
    "io.circe" %% "circe-parser" % "0.12.3",
    "org.http4s" %% "http4s-circe" % Version.http4s,
    "io.circe" %% "circe-generic-extras" % "0.12.3",
    "org.scalatest" %% "scalatest" % "3.0.8" % "test",
    "com.lendup.fs2-blobstore" %% "core" % "0.6.+",
    "com.lendup.fs2-blobstore" %% "sftp" % "0.6.+",
    "com.lendup.fs2-blobstore" %% "s3" % "0.6.+"
    )
}
