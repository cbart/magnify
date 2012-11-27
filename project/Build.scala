import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {
  val appName         = "magnify"
  val appVersion      = "0.1.0-SNAPSHOT"

  val appDependencies =
    Seq(
      "com.google.code.javaparser" % "javaparser" % "1.0.8",
      "com.google.guava" % "guava" % "13.0.1",
      "com.google.inject" % "guice" % "3.0",
      "com.google.inject.extensions" % "guice-multibindings" % "3.0",
      "com.typesafe.akka" % "akka-actor" % "2.0.3",
      "com.tinkerpop.blueprints" % "blueprints" % "2.1.0",
      "com.tinkerpop.gremlin" % "gremlin-java" % "2.1.0",
      "org.scalaz" %% "scalaz-core" % "6.0.4") ++
    Seq(  // test
      "com.typesafe.akka" % "akka-testkit" % "2.0.3" % "test",
      "junit" % "junit" % "4.11" % "test",
      "org.scalatest" %% "scalatest" % "1.8" % "test",
      "org.mockito" % "mockito-all" % "1.9.5" % "test") ++
    Seq(  // recoder
      "net.sf.retrotranslator" % "retrotranslator-runtime" % "1.2.9",
      "net.sf.retrotranslator" % "retrotranslator-transformer" % "1.2.9",
      "bsh" % "bsh" % "1.2b7",
      "backport-util-concurrent" % "backport-util-concurrent" % "3.1",
      "asm" % "asm-all" % "3.3"
    )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
    testOptions in Test := Nil,
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-Ydependent-method-types"),
    resolvers ++= Seq(
      "Maven Central" at "http://repo1.maven.org/maven2",
      "Typesafe releases" at "http://repo.typesafe.com/typesafe/releases/",
      "OSS Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/")
  )
}