import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {
  val appName         = "magnify"
  val appVersion      = "0.1.0-SNAPSHOT"

  val appDependencies = Nil

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
    testOptions in Test := Nil
  )
}