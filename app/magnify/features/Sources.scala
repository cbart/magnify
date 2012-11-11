package magnify.features

import java.util.zip.ZipFile
import magnify.model.Archive

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
trait Sources {
  def add(name: String, file: Archive)
}
