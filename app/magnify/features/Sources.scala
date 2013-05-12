package magnify.features

import magnify.model.{Json, Archive}
import magnify.model.graph.Graph
import java.io.File

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
trait Sources {

  def add(name: String, file: Archive)

  def add(name: String, graph: Json)

  def list: Seq[String]

  def get(name: String): Option[Graph]

  def getJson(name: String): Option[Json]

  def addRuntime(name: String, file: File)
}
