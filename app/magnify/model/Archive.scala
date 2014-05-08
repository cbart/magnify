package magnify.model

import java.io.InputStream
import scalaz.Monoid

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
trait Archive {

  def extract[A : Monoid](f: (String, Option[String], () => InputStream) => A): A
}
