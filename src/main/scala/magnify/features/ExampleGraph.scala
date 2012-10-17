package magnify.features

import akka.util.Duration
import cc.spray.Directives
import cc.spray.http.MediaTypes._
import cc.spray.http.MultipartFormData
import cc.spray.json._
import java.util.zip.ZipInputStream
import java.io.ByteArrayInputStream
import scala.collection.mutable.ListBuffer

trait ExampleGraph extends Directives {
  import DefaultJsonProtocol._

  val `application/gexf+xml` = register(CustomMediaType("application/gexf+xml"))

  val route = {
    path("upload.json") {
      post {
  		  respondWithMediaType(`text/html`) {
  		    content(as[MultipartFormData]) {
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
}