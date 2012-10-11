package magnify.web.api.view.json

import cc.spray.json.JsonWriter

/**
 *
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
object JsonModel {
  def apply[T](data: T, forward: String => Unit)(implicit writer: JsonWriter[T]): JsonModel[T] =
    new JsonModel[T](data, writer, forward)
}

final case class JsonModel[T] (data: T, writer: JsonWriter[T], forward: String => Unit) {
  def respond() {
    forward(writer.write(data).compactPrint)
  }
}
