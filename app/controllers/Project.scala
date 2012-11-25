package controllers

import com.tinkerpop.blueprints.Direction._
import com.tinkerpop.blueprints._
import com.tinkerpop.gremlin.pipes.filter.{LabelFilterPipe, PropertyFilterPipe}
import com.tinkerpop.pipes.filter.FilterPipe.Filter
import com.tinkerpop.pipes.filter.{FilterPipe, OrFilterPipe}
import java.lang.String
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
import scala.collection.JavaConversions._
import scalaz.Scalaz._


object Project extends Project(inject[Sources])

sealed class Project (sources: Sources) extends Controller {

  val logger = Logger(classOf[Project].getSimpleName)

  private implicit def projects: Seq[String] = sources.list

  def newProject = Action { implicit request =>
    Ok(views.html.newProject())
  }

  def show[A](name: String) = Action { implicit request =>
    sources.get(name) match {
      case Some(graph) => Ok(views.html.show(name))
      case None => Redirect(routes.Project.newProject())
          .flashing("warning" -> "Project \"%s\" was not found. Why not create new one?".format(name))
    }
  }

  def showWholeJson(name: String) =
    showJson(name, or(classes, packages), or(imports, inPackage))

  def showPackagesJson(name: String) =
    showJson(name, packages, inPackage)

  def showPkgImportsJson(name: String) =
    showJson(name, packages, or(inPackage, pkgImports))

  def showJson(name: String, nodePipe: FilterPipe[Vertex], edgePipe: FilterPipe[Edge]) = Action { implicit request =>
    sources.get(name) match {
      case Some(graph) => Ok(json(graph, nodePipe, edgePipe))
      case None => NotFound(toJson(Map("warning" -> "Project \"%s\" was not found.".format(name))))
    }
  }

  def or[S](pipes: FilterPipe[S]*): FilterPipe[S] =
      new OrFilterPipe[S](pipes: _*)

  val classes: FilterPipe[Vertex] =
    propertyFilter[Vertex]("kind", "class")

  val packages: FilterPipe[Vertex] =
    propertyFilter[Vertex]("kind", "package")

  val imports: FilterPipe[Edge] =
    new LabelFilterPipe("imports", FilterPipe.Filter.EQUAL)

  val inPackage: FilterPipe[Edge] =
    new LabelFilterPipe("in-package", FilterPipe.Filter.EQUAL)

  val pkgImports: FilterPipe[Edge] =
    new LabelFilterPipe("package-imports", FilterPipe.Filter.EQUAL)

  def propertyFilter[S <: Element](key: String, value: String): FilterPipe[S] =
    new PropertyFilterPipe[S, String](key, value, Filter.EQUAL)

  private def json(graph: Graph, nodePipe: FilterPipe[Vertex], edgePipe: FilterPipe[Edge]): JsValue = {
    val classes = graph.vertices
        .add(nodePipe)
        .toList.toSeq
    val imports = graph.edges
      .add(edgePipe)
      .toList.toSeq
    val nodes = for {
      cls <- classes.toSeq
      name = cls.getProperty("name").toString
      kind = cls.getProperty("kind").toString
    } yield {
      Map("name" -> name, "kind" -> kind)
    }
    val indices: Map[String, Int] = (for {
      (element, index) <- nodes.zipWithIndex
    } yield element("name") -> index).toMap
    val edges = for {
      edge <- imports.toSeq
      source <- indices.get(name(edge, OUT)).toSeq
      target <- indices.get(name(edge, IN)).toSeq
    } yield Map(
        "source" -> toJson(source),
        "target" -> toJson(target),
        "kind" -> toJson(edge.getLabel))
    toJson(Map(
      "nodes" -> toJson(nodes),
      "edges" -> toJson(edges)))
  }

  private def name(edge: Edge, direction: Direction): String =
    edge.getVertex(direction).getProperty("name").toString

  def upload = Action(parse.multipartFormData) { implicit request =>
    val name = request.body.dataParts.get("project-name").flatMap {
      case Seq(onlyName) => Some(onlyName)
      case _ => None
    }
    val src = request.body.file("project-sources").filter { file =>
      file.contentType.map(allowedFormats).getOrElse(false)
    }
    Logger.info(name.toString)
    Logger.info(src.toString)
    (name |@| src) { (name: String, src: FilePart[TemporaryFile]) =>
      val TemporaryFile(file) = src.ref
      sources.add(name, new Zip(file))
      name
    } match {
      case Some(name) =>
        Redirect(routes.Project.newProject())
          .flashing("success" -> "Project %s adding scheduled.".format(name))
      case None =>
        Redirect(routes.Project.newProject())
          .flashing("error" -> "Something went wrong. Project wasn't added.")
    }
  }

  private val allowedFormats = Set("application/zip", "application/x-java-archive")
}