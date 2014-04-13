import play.Project._

name := "magnify"

version := "0.2.0-SNAPSHOT"

scalaVersion := "2.10.2"

libraryDependencies ++=
  Seq(
    // TOOLs
    "com.typesafe.akka" %% "akka-actor" % "2.2.2",
    "org.scalaz" %% "scalaz-core" % "6.0.4",
    // Inject
    "com.google.guava" % "guava" % "16.0.1",
    "com.google.inject" % "guice" % "3.0",
    "com.google.inject.extensions" % "guice-multibindings" % "3.0",
    // Graph
    "com.tinkerpop.blueprints" % "blueprints" % "2.4.0",
    "com.tinkerpop.blueprints" % "blueprints-graph-jung" % "2.4.0",
    "com.tinkerpop.gremlin" % "gremlin-java" % "2.1.0",
    "net.sf.jung" % "jung-algorithms" % "2.0.1",
    // Parser
    "com.google.code.javaparser" % "javaparser" % "1.0.8",
    // Tests
    "com.typesafe.akka" %% "akka-testkit" % "2.2.1" % "test",
    "junit" % "junit" % "4.11" % "test",
    "org.scalatest" %% "scalatest" % "2.1.3" % "test",
    "org.mockito" % "mockito-all" % "1.9.5" % "test",
    // recorder
    "asm" % "asm-all" % "3.3",
    // git
    "org.eclipse.jgit" % "org.eclipse.jgit" % "3.3.1.201403241930-r"
  )

scalacOptions ++= Seq("-deprecation", "-unchecked")

resolvers ++= Seq(
  "Maven Central" at "http://repo1.maven.org/maven2",
  "Typesafe releases" at "http://repo.typesafe.com/typesafe/releases/",
  "OSS Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/")

playScalaSettings
