package magnify.services

import java.io.InputStream
import scalaz.Monoid

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
trait Reader {
  def read[A: Monoid](parse: InputStream => A): A
}
