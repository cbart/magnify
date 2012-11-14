package controllers

import com.tinkerpop.blueprints._
import com.tinkerpop.gremlin.Tokens
import magnify.features.Sources
import magnify.model.Zip
import magnify.model.graph.Graph
import magnify.modules.inject
import play.api.Logger
import play.api.libs.Files.TemporaryFile
import play.api.libs.json.Json._
import play.api.libs.json.Writes._
import play.api.libs.json._
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import scalaz.Scalaz._
import scala.collection.JavaConversions._


object Project extends Project(inject[Sources])

sealed class Project (sources: Sources) extends Controller {

  val logger = Logger(classOf[Project].getSimpleName)

  def newProject = Action { implicit request =>
    Ok(views.html.newProject())
  }

  def list = Action { implicit request =>
    Ok(views.html.list(sources.list))
  }

  def show[A](name: String) = Action { implicit request =>
    sources.get(name) match {
      case Some(graph) => Ok(views.html.show(name))
      case None => Redirect(routes.Project.list())
          .flashing("warning" -> "Project \"%s\" was not found.".format(name))
    }
  }

  def showJson[A](name: String) = Action { implicit request =>
    sources.get(name) match {
      case Some(graph) => Ok(json(graph))
      case None => NotFound(toJson(Map("warning" -> "Project \"%s\" was not found.".format(name))))
    }
  }

  private def json(graph: Graph): JsValue = {
    val classes = graph.vertices
        .has("kind", "class")
        .toList
        .toSeq
        .asInstanceOf[Seq[Vertex]]
    val imports = graph.edges
      .has(Tokens.LABEL, "imports")
      .toList
      .toSeq
      .asInstanceOf[Seq[Edge]]
    val nodes = for {
      cls <- classes.toSeq
      name = cls.getProperty("name").toString
    } yield {
      Map("name" -> name)
    }
    val indices: Map[String, Int] = (for {
      (element, index) <- nodes.zipWithIndex
    } yield element("name") -> index).toMap
    val edges = for {
      edge <- imports.toSeq
    } yield Map(
        "source" -> indices(edge.getVertex(Direction.OUT).getProperty("name").toString),
        "target" -> indices(edge.getVertex(Direction.IN).getProperty("name").toString))
    toJson(Map(
      "nodes" -> toJson(nodes),
      "edges" -> toJson(edges)))
  }

  def upload = Action(parse.multipartFormData) { implicit request =>
    val name = request.body.dataParts.get("project-name").flatMap {
      case Seq(onlyName) => Some(onlyName)
      case _ => None
    }
    val src = request.body.file("project-sources").filter { file =>
      file.contentType.map(allowedFormats).getOrElse(false)
    }
    (name |@| src) { (name: String, src: FilePart[TemporaryFile]) =>
      val TemporaryFile(file) = src.ref
      sources.add(name, new Zip(file))
      name
    } match {
      case Some(name) =>
        Redirect(routes.Project.list())
          .flashing("success" -> "Project %s adding scheduled.".format(name))
      case None =>
        Redirect(routes.Project.newProject())
          .flashing("error" -> "Something went wrong. Project wasn't added.")
    }
  }

  private val allowedFormats = Set("application/zip", "application/x-java-archive")
}