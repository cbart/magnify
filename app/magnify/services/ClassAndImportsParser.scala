package magnify.services

import java.io.InputStream

import japa.parser.{JavaParser, TokenMgrError}
import japa.parser.ast.CompilationUnit
import magnify.features.Parser
import magnify.model.Ast
import magnify.services.imports.AstBuilder
import play.api.Logger

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[services] final class ClassAndImportsParser extends Parser {
  JavaParser.setCacheParser(false)  // TODO: move this to dependency injection.

  val logger = Logger(classOf[ClassAndImportsParser].getSimpleName)

  override def apply(input: InputStream): Seq[Ast] =
    parse(input).map(AstBuilder(_)).getOrElse(Seq())

  private def parse(input: InputStream): Option[CompilationUnit] =
    try {
      Some(JavaParser.parse(new NonClosingInputStream(input)))
    } catch {
      case e: Exception =>
        logger.warn("Could not parse Java file.", e)
        None
      case e: TokenMgrError =>
        logger.warn("Lexical error", e)
        None
    }
}
