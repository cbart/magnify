package magnify.services

import java.io.InputStream

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.JavaConversions._

import japa.parser.{JavaParser, TokenMgrError}
import japa.parser.ast.{CompilationUnit, ImportDeclaration}
import japa.parser.ast.`type`.ClassOrInterfaceType
import japa.parser.ast.expr.{NameExpr, QualifiedNameExpr}
import japa.parser.ast.visitor.VoidVisitorAdapter
import magnify.features.Parser
import magnify.model.Ast
import play.api.Logger

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[services] final class ClassAndImportsParser extends Parser with OrEmptyEnhancer {
  JavaParser.setCacheParser(false)  // TODO: move this to dependency injection.

  val logger = Logger(classOf[ClassAndImportsParser].getSimpleName)

  override def apply(input: InputStream): Seq[Ast] =
    parse(input).map(new AstBuilder(_)).map((astBuilder) => {
      for {
        className <- getClassNames(astBuilder.unit)
      } yield (astBuilder.get(className))
    }).getOrElse(Seq())

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

  private def getClassNames(unit: CompilationUnit): Seq[String] =
    for {
      typ <- unit.getTypes.orEmpty
      if typ.getName ne null
    } yield typ.getName
}

private[this] class AstBuilder(val unit: CompilationUnit) extends VoidVisitorAdapter[Object] with OrEmptyEnhancer {

  val logger = Logger(classOf[AstBuilder].getSimpleName)

  private val imports = getImports((anyImport) => !anyImport.isAsterisk)
  private val importedClasses = imports.map((fullImport) => fullImport.split("\\.").last).toSet
  private val prefix = packagePrefix
  private val asteriskPackages = getImports((anyImport) => anyImport.isAsterisk) ++ Seq(prefix.mkString("."))
  private val unresolvedClasses = mutable.ListBuffer[String]()

  private val javaLangClasses = Set(
    "String", "Double", "Float", "Integer", "Boolean", "Exception", "RuntimeException", "Byte", "Character", "Class",
    "ClassLoader", "Long", "Object", "Short", "StringBuffer", "StringBuilder", "System", "Thread", "Throwable")

  visit(unit, null);

  def get(className: String): Ast = Ast(
    imports,
    (prefix :+ className).mkString("."),
    asteriskPackages,
    unresolvedClasses.toSeq)

  override def visit(n: ClassOrInterfaceType, arg: Object) = {
    if (!importedClasses.contains(n.getName) && !javaLangClasses.contains(n.getName)) {
      logger.debug("unresolved dependency: " + n.getName)
      unresolvedClasses += n.getName
    }
  }

  private def getImports(filter: (ImportDeclaration => Boolean)): Seq[String] =
    unit.getImports.orEmpty.filter((anyImport) => {
      !anyImport.isStatic && filter(anyImport)
    }).map((anyImport) => {
      extractName(anyImport.getName).mkString(".")
    })

  private def packagePrefix: Seq[String] =
    if (unit.getPackage ne null) {
      extractName(unit.getPackage.getName)
    } else {
      Seq()
    }

  @tailrec
  private def extractName(expr: NameExpr, prefix: List[String] = List.empty[String]): Seq[String] =
    expr match {
      case e: QualifiedNameExpr => extractName(e.getQualifier, e.getName :: prefix)
      case e: NameExpr => e.getName :: prefix
    }
}

private trait OrEmptyEnhancer {
  implicit class WithOrEmpty[A](value: java.util.List[A]) {
    def orEmpty: Seq[A] = Option(value).map(_.toSeq).getOrElse(Seq())
  }
}
