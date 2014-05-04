package controllers


import magnify.model.graph.Graph

object Committers {
  def apply(revision: Option[String], graph: Graph): Set[Map[String, String]] = {
    val commits = graph.commitsOlderThan(revision)
    commits.map((vrtx) => {
      val authorWithTime = vrtx.getProperty[String]("author")
      Map("name" -> getName(authorWithTime))
    }).toSet
  }

  def getName(authorWithTime: String): String = {
    val endEmailIndex = authorWithTime.lastIndexOf('>')
    authorWithTime.substring(0, endEmailIndex + 1)
  }
}
