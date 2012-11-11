package magnify.features

import java.io.InputStream
import magnify.model.Ast

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
trait Parser extends (InputStream => Seq[Ast])
