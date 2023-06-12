name := "udemy-akka-essentials"

version := "0.1"

scalaVersion := "2.13.8"

val akkaVersion = "2.6.18"
val scalaTestVersion = "3.2.9"
val logbackVersion = "1.2.10"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion,
)