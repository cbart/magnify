package magnify.services.imports

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.JavaConversions._

import japa.parser.ast.{CompilationUnit, ImportDeclaration}
import japa.parser.ast.`type`.ClassOrInterfaceType
import japa.parser.ast.body.TypeDeclaration
import japa.parser.ast.expr.{NameExpr, QualifiedNameExpr}
import japa.parser.ast.visitor.VoidVisitorAdapter
import magnify.model.Ast
import play.api.Logger

class AstBuilder(
    packagePrefix: Seq[String],
    typeName: String,
    imports: Seq[String],
    asteriskPackages: Seq[String])
  extends VoidVisitorAdapter[Object]
  with (TypeDeclaration => Ast)
  with OrEmptyEnhancer {
  val logger = Logger(classOf[AstBuilder].getSimpleName)

  private val classToImportedQualifiedName =
    imports.map((fullImport) => (fullImport.split("\\.").last, fullImport)).toMap
  private val unresolvedClasses = mutable.Set[String]()
  private val qualifiedNamesOfUsedClasses = mutable.Set[String]()

  private val javaLangClasses = Set(
    "String", "Double", "Float", "Integer", "Boolean", "Exception", "RuntimeException", "Byte", "Character", "Class",
    "ClassLoader", "Long", "Object", "Short", "StringBuffer", "StringBuilder", "System", "Thread", "Throwable")

  private def addPossibleClassDependency(className: String): Boolean =
    if (javaLangClasses.contains(className)) { true } else {
      if (classToImportedQualifiedName.contains(className)) {
        qualifiedNamesOfUsedClasses += classToImportedQualifiedName(className)
        true
      } else {
        logger.debug("unresolved dependency: " + className)
        unresolvedClasses += className
        false
      }
    }

  private def isInnerOrQualifiedName(n: ClassOrInterfaceType): Boolean = n.getScope != null

  @tailrec
  private def getFullyQualifiedName(n: ClassOrInterfaceType, sufix: List[String] = List.empty[String]): String =
    if (isInnerOrQualifiedName(n)) {
      getFullyQualifiedName(n.getScope, n.getName :: sufix)
    } else {
      (n.getName :: sufix).mkString(".")
    }

  override def visit(n: ClassOrInterfaceType, arg: Object) = {
    if (isInnerOrQualifiedName(n)) {
      val name = getFullyQualifiedName(n)
      // try inner class
      if (!addPossibleClassDependency(name.split("\\.").head)) {
        addPossibleClassDependency(name) // fully qualified name
      }
    } else {
      addPossibleClassDependency(n.getName)
    }
  }

  override def apply(typeUnit: TypeDeclaration): Ast = {
    typeUnit.accept(this, null)
    Ast(
      (packagePrefix :+ typeName).mkString("."),
      qualifiedNamesOfUsedClasses.toSet,
      asteriskPackages.toSet,
      unresolvedClasses.toSet)
  }

}

object AstBuilder extends OrEmptyEnhancer {

  def apply(unit: CompilationUnit): Seq[Ast] = {
    val prefix = packagePrefix(unit)
    val explicitImports = getImports(unit, (anyImport) => !anyImport.isAsterisk)
    val asteriskImports = getImports(unit, (anyImport) => anyImport.isAsterisk) ++
        (if (prefix.nonEmpty) Set(prefix.mkString(".")) else Set())
    Seq((getDeclaredTypes(unit).map { typeUnit =>
      val astBuilder = new AstBuilder(
        prefix,
        typeUnit.getName,
        explicitImports,
        asteriskImports)
      astBuilder(typeUnit)
    }): _*)
  }

  private def getDeclaredTypes(unit: CompilationUnit): Seq[TypeDeclaration] =
    unit.getTypes.orEmpty.filter(_.getName ne null)

  private def getImports(unit: CompilationUnit, filter: (ImportDeclaration => Boolean)): Seq[String] =
    unit.getImports.orEmpty.filter((anyImport) => {
      !anyImport.isStatic && filter(anyImport)
    }).map((anyImport) => {
      extractName(anyImport.getName).mkString(".")
    })

  private def packagePrefix(unit: CompilationUnit): Seq[String] =
    if (unit.getPackage ne null) {
      extractName(unit.getPackage.getName)
    } else {
      Seq()
    }
}

trait OrEmptyEnhancer {
  implicit class WithOrEmpty[A](value: java.util.List[A]) {
    def orEmpty: Seq[A] = Option(value).map(_.toSeq).getOrElse(Seq())
  }

  @tailrec
  final def extractName(expr: NameExpr, prefix: List[String] = List.empty[String]): Seq[String] =
    expr match {
      case e: QualifiedNameExpr => extractName(e.getQualifier, e.getName :: prefix)
      case e: NameExpr => e.getName :: prefix
    }
}
