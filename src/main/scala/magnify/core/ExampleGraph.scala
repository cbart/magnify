package magnify.core

import com.tinkerpop.blueprints.Graph
import com.tinkerpop.blueprints.impls.tg.TinkerGraph
import akka.util.Duration
import akka.util.duration.intToDurationInt
import cc.spray.Directives
import cc.spray.http.MediaTypes._
import cc.spray.http.MultipartFormData
import cc.spray.json._
import java.util.zip.ZipInputStream
import java.io.ByteArrayInputStream
import scala.collection.mutable.ListBuffer

trait ExampleGraph extends Directives {
  import Gexf._
  import DefaultJsonProtocol._

  val `application/gexf+xml` = register(CustomMediaType("application/gexf+xml"))

  val route = {
    path("graph.gexf") {
      respondWithMediaType(`application/gexf+xml`) {
        completeWith(exampleGraph.toXml)
      }
    } ~
    path("upload.json") {
      post {
  		  respondWithMediaType(`text/html`) {
  		    content(as[MultipartFormData]) {
  		      def extractOriginalText(formData: MultipartFormData): String = {
  		        formData.parts.values.map { bodyPart =>
  		          bodyPart.content.map(content => new String(content.buffer))
  		        }.flatten.mkString
  		      }
  		      formData => _.complete(extractor(formData))
  		    }
  		  }
      }
    }
  }

  def extract[T, U](parse: Array[Byte] => T, merge: Traversable[T] => U)
      (formData: MultipartFormData): U = {
    merge(formData.parts.values.map { body =>
      body.content.map(content => parse(content.buffer))
    }.flatten)
  }

  def toZipStream(bytes: Array[Byte]): ZipInputStream =
    new ZipInputStream(new ByteArrayInputStream(bytes))

  def listFilesAndClose(zip: ZipInputStream): Seq[String] = {
    var entry = zip.getNextEntry
    val fileNames = ListBuffer.empty[String]
    while (entry ne null) {
      fileNames += entry.getName
      zip.closeEntry
      entry = zip.getNextEntry
    }
    fileNames.toSeq
  }

  val extractor = extract(toZipStream, { (files: Traversable[ZipInputStream]) =>
    (for {
      zip <- files.view
      file <- listFilesAndClose(zip).view
      if file.endsWith(".java") && !file.endsWith("Test.java")
    } yield file).force.toList.toJson.compactPrint
  }) _

  def in[U](duration: Duration)(body: => U) {
    actorSystem.scheduler.scheduleOnce(duration, new Runnable { def run() { body } })
  }

  private val numberOfNodes = 100
  private val numberOfEdges = 300

  private def exampleGraph: Graph = {
    val random = new scala.util.Random()
    val graph = new TinkerGraph()
    val nodes = 0 until numberOfNodes map { _ => graph.addVertex(null) }
    (0 until numberOfEdges) foreach { _ =>
      val source = nodes(random.nextInt.abs % numberOfNodes)
      val destination = nodes(random.nextInt.abs % numberOfNodes)
      graph.addEdge(null, source, destination, random.nextString(2))
    }
    graph
  }
}