package magnify.services

import magnify.features.Imports
import magnify.model.Ast
import play.api.Logger

private[services] final class ExplicitProjectImports extends Imports {
  val logger = Logger(classOf[ExplicitProjectImports].getSimpleName)

  /**
   * Resolves only explicit imports as:
   *
   * {{{
   *   import magnify.model.Ast
   * }}}
   *
   * Does not resolve implicit "same package imports", asterisk imports and static imports.
   */
  override def resolve(classes: Iterable[Ast]): Map[String, Seq[String]] = {
    val classNames = classes.map(_.className).toSet
    val imports = for {
      Ast(imports, name, asteriskPackages, unresolvedClasses) <- classes
    } yield {
      val possibleImports = for (
        packageName <- asteriskPackages;
        className <- unresolvedClasses
      ) yield (packageName + "." + className)
      val implicitImports = possibleImports.filter(classNames)
      logger.debug("implicitImports in " + name + " : " + implicitImports.mkString(", "))
      (name, imports.filter(classNames) ++ implicitImports)
    }
    imports.toMap
  }
}
