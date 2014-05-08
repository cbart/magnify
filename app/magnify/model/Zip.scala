package magnify.model

import java.io._
import java.util.zip.ZipInputStream
import magnify.services.NonClosingInputStream
import scala.annotation.tailrec
import scalaz.Monoid

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
final class Zip (file: File) extends Archive {
  override def extract[A: Monoid](f: (String, Option[String], () => InputStream) => A): A = {
    val input = new FileInputStream(file)
    val zip = new ZipInputStream(new BufferedInputStream(input))
    val monoid = implicitly[Monoid[A]]
    try {
      fold(monoid.zero, zip, (acc: A, name: String, content: InputStream) =>
        monoid.append(acc, f(name, None, () => content)))
    } finally {
      close(zip)
      close(input)
    }
  }

  @tailrec
  private def fold[A](acc: A, stream: ZipInputStream, transform: (A, String, InputStream) => A): A = {
    Option(stream.getNextEntry) match {
      case Some(entry) =>
        // NonClosingInputStream - only for defense. Method accepting an InputStream via loaner pattern
        // should _not_ close it. Ever. That's the responsibility of the loaner.
        fold(transform(acc, entry.getName, new NonClosingInputStream(stream)), stream, transform)
      case None =>
        acc
    }
  }

  private def close(resource: Closeable) {
    try {
      resource.close()
    } catch {
      case e: Exception => ()  // TODO: Log
    }
  }
}
