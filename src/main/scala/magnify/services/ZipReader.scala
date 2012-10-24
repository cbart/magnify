package magnify.services

import java.io.{ByteArrayInputStream, BufferedInputStream, InputStream}
import java.util.zip.ZipInputStream
import scala.collection.mutable

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
final class ZipReader (input: Array[Byte], fileSelector: String => Boolean) extends SourceReader {
  def flatMap[A](f: (InputStream) => Seq[A]): Seq[A] = {
    val buffer = mutable.ListBuffer.empty[A]
    foreachEntry {
      inputStream => buffer ++= f(inputStream)
    }
    buffer.toList
  }

  private def foreachEntry(f: InputStream => Unit) {
    val bytes = new ByteArrayInputStream(input)
    val zip = new ZipInputStream(new BufferedInputStream(bytes))
    try {
      read(zip, f)
    } finally {
      zip.close()
      bytes.close()
    }
  }

  private def read(zip: ZipInputStream, f: InputStream => Unit) {
    var entry = zip.getNextEntry
    while (null ne entry) {
      if (fileSelector(entry.getName)) {
        f(zip)
      }
      entry = zip.getNextEntry
    }
  }
}
