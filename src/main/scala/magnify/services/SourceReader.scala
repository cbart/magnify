package magnify.services

import java.io.InputStream

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
trait SourceReader {
  def flatMap[A](f: InputStream => Seq[A]): Seq[A]
}
