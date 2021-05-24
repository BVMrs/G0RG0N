name := "G0RG0N"

version := "0.1"

scalaVersion := "2.13.5"

val fs2Version = "3.0.0"
//val http4sVersion = "0.21.18"
val circeVersion = "0.13.0"

libraryDependencies ++= Seq(
  "co.fs2" %% "fs2-core" % fs2Version,
  "co.fs2" %% "fs2-io" % fs2Version,
//  "org.http4s" %% "http4s-dsl" % http4sVersion,
//  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
//  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
//  "org.http4s" %% "http4s-circe" % http4sVersion,
  "io.circe" %% "circe-config" % "0.8.0",
  "io.chrisdavenport" %% "log4cats-slf4j" % "1.1.1",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion
)

fork in run := true
outputStrategy := Some(StdoutOutput)
connectInput in run := true

scalacOptions ++= List(
  "-feature",
  "-language:higherKinds",
  "-Xlint",
  "-Yrangepos",
  "-Ywarn-unused"
)

addCompilerPlugin("org.typelevel" % "kind-projector" % "0.11.3" cross CrossVersion.full)

// if your project uses both 2.10 and polymorphic lambdas
libraryDependencies ++= (scalaBinaryVersion.value match {
  case "2.10" =>
    compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full) :: Nil
  case _ =>
    Nil
})

idePackagePrefix := Some("com.bvmrs.bckch")
