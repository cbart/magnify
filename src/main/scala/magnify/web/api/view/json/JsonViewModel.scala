package magnify.web.api.view.json

import cc.spray.json.JsonWriter
import magnify.web.api.view.ViewModel

/**
 *
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
object JsonViewModel {
  def apply[T](data: T, forward: String => Unit)(implicit writer: JsonWriter[T]): JsonViewModel[T] =
    new JsonViewModel[T](data, writer, forward)
}

final case class JsonViewModel[T] (data: T, writer: JsonWriter[T], forward: String => Unit)
    extends ViewModel {
  override def respond() {
    forward(writer.write(data).compactPrint)
  }
}
