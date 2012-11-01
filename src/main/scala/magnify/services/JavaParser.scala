package magnify.services

import java.io.InputStream
import magnify.model.java.Ast

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
trait JavaParser extends (InputStream => Seq[Ast]) {
  def parse(input: InputStream): Seq[Ast]

  override final def apply(input: InputStream): Seq[Ast] =
    parse(input)
}
