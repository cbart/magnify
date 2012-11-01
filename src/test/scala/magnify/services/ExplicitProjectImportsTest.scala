package magnify.services

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import magnify.model.java.Ast
import org.scalatest.matchers.ShouldMatchers
import magnify.model.graph.Graph

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
}
