package magnify.services

import java.io.InputStream

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
trait SourceReader {
  def map[A](f: InputStream => A): Seq[A]
}
