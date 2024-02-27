ThisBuild / scalaVersion     := "2.13.10"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "ru.mikhaildruzhinin"
ThisBuild / organizationName := "mikhaildruzhinin"

resolvers += "Secured Central Repository" at "https://repo.maven.apache.org/maven2"

externalResolvers := Resolver.combineDefaultResolvers(resolvers.value.toVector, mavenCentral = false)

lazy val root = (project in file("."))
  .settings(
    name := "space-traders"
  )

val scalatestVersion = "3.2.17"
val sttpVersion = "3.9.1"
val circeVersion = "0.14.6"
val enumeratumVersion = "1.7.3"

libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % scalatestVersion,
  "org.scalatest" %% "scalatest" % scalatestVersion % "test",
  "com.github.pureconfig" %% "pureconfig" % "0.17.4",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "ch.qos.logback" % "logback-classic" % "1.4.7" % Runtime,
  "com.softwaremill.sttp.client3" %% "core" % sttpVersion,
  "com.softwaremill.sttp.client3" %% "circe" % sttpVersion,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "com.beachape" %% "enumeratum" % enumeratumVersion,
  "com.beachape" %% "enumeratum-circe" % enumeratumVersion,
  "com.softwaremill.macwire" % "macros_2.13" % "2.5.9"
)
