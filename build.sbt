organization := "magnify"

name := "magnify"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.10.0-RC1"

resolvers ++= Seq(
  "Typesafe repo" at "http://repo.typesafe.com/typesafe/releases/",
  "Spray repo" at "http://repo.spray.cc/",
  "OSS Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "OSS Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/")

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")

libraryDependencies ++= Seq(
  "io.spray" % "spray-routing" % "1.1-M4.1",
  "io.spray" % "spray-can" % "1.1-M4.1",
  "io.spray" %% "spray-json" % "1.2.2",
  "io.spray" % "spray-testkit" % "1.1-M4.1" % "test",
  "com.google.inject" % "guice" % "3.0",
  "com.google.inject.extensions" % "guice-multibindings" % "3.0",
  "com.typesafe.akka" %% "akka-actor" % "2.1.0-RC1",
  "com.typesafe.akka" %% "akka-testkit" % "2.1.0-RC1" % "test",
  "com.tinkerpop.blueprints" % "blueprints-core" % "2.1.0",
  "junit" % "junit" % "4.10" % "test",
  "org.scala-lang" % "scala-actors" % "2.10.0-RC1",  // fix: scalatest runs on old actors
  "org.scala-lang" % "scala-reflect" % "2.10.0-RC1", // ditto
  "org.scalatest" %% "scalatest" % "2.0.M4" % "test",
  "org.mockito" % "mockito-all" % "1.9.0" % "test")

assemblySettings
