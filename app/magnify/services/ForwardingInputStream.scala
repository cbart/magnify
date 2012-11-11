package magnify.services

import java.io.InputStream

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
abstract class ForwardingInputStream (delegate: InputStream) extends InputStream {
  override def read: Int =
    delegate.read

  override def read(bytes: Array[Byte]): Int =
    delegate.read(bytes)

  override def read(bytes: Array[Byte], offset: Int, length: Int): Int =
    delegate.read(bytes, offset, length)

  override def skip(n: Long): Long =
    delegate.skip(n)

  override def available: Int =
    delegate.available

  override def close() {
    delegate.close()
  }

  override def mark(readLimit: Int) {
    delegate.mark(readLimit)
  }

  override def reset() {
    delegate.reset()
  }

  override def markSupported: Boolean =
    delegate.markSupported
}
