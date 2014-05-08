package magnify.features

import java.io.File

import magnify.model.{Archive, Json, SingleVersionArchive, VersionedArchive}
import magnify.model.graph.Graph

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
trait Sources {

  def add(name: String, archive: VersionedArchive): Unit

  def add(name: String, archive: Archive): Unit = this.add(name, new SingleVersionArchive(archive))

  def add(name: String, graph: Json): Unit

  def list: Seq[String]

  def get(name: String): Option[Graph]

  def getJson(name: String): Option[Json]

  def addRuntime(name: String, file: File)
}
