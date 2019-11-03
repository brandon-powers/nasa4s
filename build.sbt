import Version._

name := "nasa4s"
version := "0.1"
scalaVersion := Version.scala
scalacOptions in ThisBuild ++= Seq("-feature", "-language:higherKinds")

lazy val commonSettings = Seq(
  version := "0.1",
  scalaVersion := Version.scala,
  addCompilerPlugin(CompilerPlugins.paradise),
  scalacOptions ++= Seq(
    "-Ywarn-value-discard",
    "-Xfatal-warnings",
    "-Ypartial-unification",
    "-Ywarn-unused:imports"
  )
)

lazy val `nasa4s-apod` = (project in file("apod"))
  .settings(
    commonSettings,
    name := "nasa4s-apod",
    libraryDependencies ++= Dependencies.`nasa4s-apod`
  )

lazy val `nasa4s-neows` = (project in file("neows"))
  .settings(
    commonSettings,
    name := "nasa4s-neows",
    libraryDependencies ++= Dependencies.`nasa4s-neows`
  )

