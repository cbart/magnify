package magnify.services

import java.io.InputStream

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
abstract class ForwardingInputStream (delegate: InputStream) extends InputStream {
  override def read(): Int = delegate.read()

  override def read(b: Array[Byte]) = delegate.read(b)

  override def read(b: Array[Byte], off: Int, len: Int) = delegate.read(b, off, len)

  override def skip(n: Long) = delegate.skip(n)

  override def available() = delegate.available()

  override def close() {
    delegate.close()
  }

  override def mark(readlimit: Int) {
    delegate.mark(readlimit)
  }

  override def reset() {
    delegate.reset()
  }

  override def markSupported() = delegate.markSupported()
}
