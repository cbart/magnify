package magnify.services

import magnify.model.Ast
import org.junit.runner.RunWith
import org.scalatest.{FunSuite, Matchers}
import org.scalatest.junit.JUnitRunner

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
@RunWith(classOf[JUnitRunner])
final class ProjectImportsTest extends FunSuite with Matchers {
  val imports = new ProjectImports()

  test("should produce who-imports-who mapping refering only to project classes") {
    val classes = Ast("test.Local1", Set("test.Local2", "external.Class"), Set(), Set()) ::
        Ast("test.Local2", Set("test.Local1"), Set(), Set()) :: Nil
    val resolutions = imports.resolve(classes)
    resolutions should equal(Map("test.Local1" -> Seq("test.Local2"),
      "test.Local2" -> Seq("test.Local1")))
  }

  test("should note classes even if they don't import anything") {
    val classes = Ast("test.Local", Set(), Set(), Set()) :: Nil
    val resolutions = imports.resolve(classes)
    resolutions should equal(Map("test.Local" -> Seq()))
  }

  test("should produce who-imports-who mapping for asterisk imports") {
    val classes = Ast("testA.Local1", Set("Invalid.Class"), Set("testB"), Set("Local2")) ::
        Ast("testB.Local2", Set(), Set("testA"), Set("Local1")) :: Nil
    val resolutions = imports.resolve(classes)
    resolutions should equal(Map("testA.Local1" -> Seq("testB.Local2"),
      "testB.Local2" -> Seq("testA.Local1")))
  }

  test("should produce who-imports-who mapping for fully qualified names") {
    val classes = Ast("testA.Local1", Set("Invalid.Class"), Set(), Set()) ::
        Ast("testB.Local2", Set(), Set(), Set("testA.Local1")) :: Nil
    val resolutions = imports.resolve(classes)
    resolutions should equal(Map("testA.Local1" -> Seq(),
      "testB.Local2" -> Seq("testA.Local1")))
  }
}
