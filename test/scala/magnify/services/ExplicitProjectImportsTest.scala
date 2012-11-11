package magnify.services

import magnify.model.Ast
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
@RunWith(classOf[JUnitRunner])
final class ExplicitProjectImportsTest extends FunSuite with ShouldMatchers {
  val imports = new ExplicitProjectImports()

  test("should produce who-imports-who mapping refering only to project classes") {
    val classes = Ast(Seq("test.Local2", "external.Class"), "test.Local1") ::
        Ast(Seq("test.Local1"), "test.Local2") :: Nil
    val resolutions = imports.resolve(classes)
    resolutions should equal(Map("test.Local1" -> Seq("test.Local2"),
      "test.Local2" -> Seq("test.Local1")))
  }

  test("should note classes even if they don't import anything") {
    val classes = Ast(Seq(), "test.Local") :: Nil
    val resolutions = imports.resolve(classes)
    resolutions should equal(Map("test.Local" -> Seq()))
  }
}
