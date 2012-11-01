package magnify.services

import java.io.InputStream

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
final class NonClosingInputStream (delegate: InputStream) extends ForwardingInputStream(delegate) {
  override def close() {
  }
}
