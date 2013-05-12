package magnify.model

import java.io.File
import scala.io.Source

/**
 * @author Cezary Bartoszuk (cezary@codilime.com)
 */
class Json (file: File) {

  def getContents: String =
    Source.fromFile(file).getLines().mkString("\n")
}
