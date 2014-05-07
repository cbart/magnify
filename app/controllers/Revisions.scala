package controllers

import java.text.SimpleDateFormat
import java.util.Date

import magnify.model.graph.Graph

object Revisions {

  val format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")

  def apply(revision: Option[String], graph: Graph): Seq[Map[String, String]] = {
    val commits = graph.commitsOlderThan(revision)
    commits.map { (vrtx) =>
      val rev = vrtx.getProperty[String]("rev")
      val desc = vrtx.getProperty[String]("desc")
      val author = Committers.getName(vrtx.getProperty[String]("author"))
      val committer = Committers.getName(vrtx.getProperty[String]("committer"))
      val time = format.format(new Date(vrtx.getProperty[Integer]("time") * 1000L))
      Map("id" -> rev, "description" -> desc, "author" -> author, "committer" -> committer, "time" -> time)
    }
  }
}
