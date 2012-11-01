package magnify.web.api.controllers

import spray.routing._
import scalaz.Scalaz._
import magnify.services.{JavaParser, Reader}
import magnify.features.SourceUseCase

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
class DataController(
    sources: SourceUseCase,
    unzip: (Array[Byte], String => Boolean) => Reader,
    parser: JavaParser) {
  def uploadZipSrc(name: String, bytes: Array[Byte]): RequestContext => Unit = {
    val reader = unzip(bytes, onlyJavaFiles)
    sources.add(name, reader.read(parser))
    _.complete("OK")
  }

  private val onlyJavaFiles: String => Boolean = {
    filename => filename.endsWith(".java") && !filename.endsWith("Test.java")
  }
}
