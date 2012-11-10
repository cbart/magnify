package magnify.features

import magnify.services.Reader

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
trait SourceUseCase {
  def add(name: String, sources: Reader)
}
