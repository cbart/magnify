package magnify.web.api.controllers

import magnify.features.{GraphUseCase, SourceUseCase}
import magnify.services.Reader
import magnify.web.api.view.Gexf

import spray.http.StatusCodes
import spray.routing._

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
class DataController(
    graphs: GraphUseCase,
    sources: SourceUseCase,
    unzip: (Array[Byte], String => Boolean) => Reader) {

  def uploadZipSrc(name: String, bytes: Array[Byte]): RequestContext => Unit = {
    sources.add(name, unzip(bytes, onlyJavaFiles))
    _.complete(StatusCodes.OK)
  }

  def showGraph(name: String): RequestContext => Unit = _.complete {
    graphs.get(name) match {
      case Some(graph) => new Gexf(graph).toXml
      case None => <error>No graph found</error>
    }
  }

  private val onlyJavaFiles: String => Boolean = {
    filename => filename.endsWith(".java") && !filename.endsWith("Test.java")
  }
}
