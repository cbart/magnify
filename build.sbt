organization := "magnify"

name := "magnify"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.9.1"

resolvers ++= Seq(
  "Typesafe repo" at "http://repo.typesafe.com/typesafe/releases/",
  "Spray repo" at "http://repo.spray.cc/")

scalacOptions ++= Seq("-unchecked", "-deprecation")

libraryDependencies ++= Seq(
  "cc.spray" % "spray-server" % "1.0-M1",
  "cc.spray" % "spray-can" % "1.0-M1",
  "cc.spray" %% "spray-json" % "1.1.1",
  "com.typesafe.akka" % "akka-actor" % "2.0.1",
  "com.typesafe.akka" % "akka-testkit" % "2.0.1",
  "com.tinkerpop.blueprints" % "blueprints-core" % "2.1.0",
  "junit" % "junit" % "4.10",
  "org.scalatest" %% "scalatest" % "1.8")

assemblySettings
