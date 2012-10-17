package magnify.web.api.routes

import akka.actor.{ActorRef, ActorSystem}
import cc.spray.{Directives, Route}
import cc.spray.directives.{PathEnd, PathElement}
import cc.spray.http.HttpMethods.{GET, PUT, POST}
import com.google.inject.{Provider, Inject}
import com.google.inject.name.Named
import com.tinkerpop.blueprints.Graph
import cc.spray.http.MediaTypes._
import magnify.web.api.view.gexf.GexfViewModel
import cc.spray.RequestContext
import magnify.features.project.graph.GetProjectGraph
import cc.spray.http.MultipartFormData
import java.util.zip.ZipInputStream
import java.io.ByteArrayInputStream
import scala.collection.mutable.ListBuffer
import cc.spray.json._
import scala.io.Source

/**
 * Data manipulation REST routes.
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[routes] final class Data @Inject() (actorSystem: ActorSystem,
    @Named("project-graph") projectGraph: ActorRef,
    @Named("view") gexfView: ActorRef) extends Provider[Route] {
  import DefaultJsonProtocol._

  val directives = Directives(actorSystem)

  import directives._

  override def get: Route = {
    pathPrefix("data" / "projects") {
      (path("list.json") & method(GET)) { context =>
        completeWith("LIST.JSON")
      } ~
      pathPrefix(PathElement) { project =>
        pathPrefix("head") {
          (path("whole.gexf") & method(GET)) { context =>
            projectGraph ! GetProjectGraph(project, renderGexfTo(context) _)
          } ~
          (path("calls") & method(PUT)) {
            completeWith("uploaded calls.csv for %s".format(project))
          }
        }
      } ~
      (path(PathEnd) & method(POST)) {
        respondWithMediaType(`text/html`) {
          content(as[MultipartFormData]) {
            formData => _.complete(extractor(formData))
          }
        }
      }
    }
  }

  def renderGexfTo(context: RequestContext)(graph: Graph) {
    gexfView ! GexfViewModel(graph, context complete _)
  }

  def extract[T, U](parse: Array[Byte] => T, merge: Traversable[T] => U)
      (formData: MultipartFormData): U = {
    merge(formData.parts.values.map { body =>
      body.content.map(content => parse(content.buffer))
    }.flatten)
  }

  def toZipStream(bytes: Array[Byte]): ZipInputStream =
    new ZipInputStream(new ByteArrayInputStream(bytes))

  def listFilesAndClose(includeFile: String => Boolean,
      zip: ZipInputStream): Map[String, String] = {
    var entry = zip.getNextEntry
    val files = ListBuffer.empty[(String, String)]
    while (entry ne null) {
      if (includeFile(entry.getName)) {
        files += ((entry.getName, Source.fromInputStream(zip).getLines.mkString("\n")))
      }
      zip.closeEntry
      entry = zip.getNextEntry
    }
    files.toMap
  }

  val javaFile: String => Boolean = {
    fileName => (fileName.endsWith(".java") && !fileName.endsWith("Test.java")
      && !fileName.endsWith("package-info.java"))
  }

  val extractor = extract(toZipStream, { (files: Traversable[ZipInputStream]) =>
    (for {
      zip <- files.view
      file <- listFilesAndClose(javaFile, zip).keys.view
    } yield file).force.toList.toJson.compactPrint
  }) _
}