organization := "magnify"

name := "magnify"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.9.1"

resolvers ++= Seq(
  "Typesafe releases" at "http://repo.typesafe.com/typesafe/releases/",
  "OSS Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "OSS Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Gephi Releases" at "http://nexus.gephi.org/nexus/content/repositories/releases/",
  "Gephi Snapshots" at "http://nexus.gephi.org/nexus/content/repositories/snapshots/",
  "Gephi Thirdparty" at "http://nexus.gephi.org/nexus/content/repositories/thirdparty/")

scalacOptions ++= Seq("-deprecation", "-unchecked", "-Ydependent-method-types")

libraryDependencies ++= Seq(
  "com.google.code.javaparser" % "javaparser" % "1.0.8",
  "com.google.guava" % "guava" % "13.0.1",
  "com.google.inject" % "guice" % "3.0",
  "com.google.inject.extensions" % "guice-multibindings" % "3.0",
  "com.typesafe.akka" % "akka-actor" % "2.0.3",
  "com.tinkerpop.blueprints" % "blueprints" % "2.1.0",
  "com.tinkerpop.gremlin" % "gremlin-java" % "2.1.0",
  //"org.gephi" % "gephi-toolkit" % "0.8.2-SNAPSHOT",
  "org.scala-sbt" % "sbt" % "0.12.1",
  "org.scalaz" %% "scalaz-core" % "6.0.4")

// Test
libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-testkit" % "2.0.3" % "test",
  "junit" % "junit" % "4.10" % "test",
  "org.scalatest" %% "scalatest" % "1.8" % "test",
  "org.mockito" % "mockito-all" % "1.9.0" % "test")
