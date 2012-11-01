package magnify.services

import scalaz.Monoid

import java.io.{ByteArrayInputStream, BufferedInputStream, InputStream}
import java.util.zip.ZipInputStream

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
final class ZipReader (input: Array[Byte], fileSelector: String => Boolean) extends Reader {
  override def read[A: Monoid](parse: InputStream => A): A = {
    val monoid = implicitly[Monoid[A]]
    fold(monoid.zero, (acc: A, input: InputStream) => monoid.append(acc, parse(input)))
  }

  private def fold[A](zero: A, join: (A, InputStream) => A): A = {
    val bytes = new ByteArrayInputStream(input)
    val zip = new ZipInputStream(new BufferedInputStream(bytes))
    try {
      var entry = zip.getNextEntry
      var accumulator = zero
      while (entry ne null) {
        if (fileSelector(entry.getName)) {
          accumulator = join(accumulator, zip)
        }
        zip.closeEntry()
        entry = zip.getNextEntry
      }
      accumulator
    } finally {
      zip.close()
      bytes.close()
    }
  }
}
