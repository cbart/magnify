package magnify.services

import java.io.ByteArrayInputStream

import magnify.features.Parser
import magnify.model.Ast
import org.junit.runner.RunWith
import org.scalatest.{FunSuite, Matchers}
import org.scalatest.junit.JUnitRunner

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
@RunWith(classOf[JUnitRunner])
final class ClassAndImportsParserTest extends FunSuite with Matchers {
  val parser: Parser = new ClassAndImportsParser()

  test("should parse simple class with no imports yielding fully qualified name") {
    val source =
      """
        |package magnify.mock.classes;
        |
        |class Example {
        |};
      """.stripMargin
    parse(source) should equal(Seq(Ast("magnify.mock.classes.Example", Set(), Set("magnify.mock.classes"), Set())))
  }

  test("should parse two classes with singleton package") {
    val source =
      """
        |package test;
        |
        |public class Example1 {
        |};
        |private class Example2 {
        |};
      """.stripMargin
    parse(source) should equal(Seq(
      Ast("test.Example1", Set(), Set("test"), Set()),
      Ast("test.Example2", Set(), Set("test"), Set())))
  }

  test("should parse used imports into fully qualified names") {
    val source =
      """
        |import test.inner.Cls;
        |
        |class Local {
        |
        |  Cls getCls();
        |};
      """.stripMargin
    parse(source) should equal(Seq(Ast("Local", Set("test.inner.Cls"), Set(), Set())))
  }

  test("should parse fully qualified names and inner classes") {
    val source =
      """
        |import test.inner.Cls;
        |
        |class Local {
        |
        |  Cls.Inner getInner();
        |  java.util.List getList();
        |};
      """.stripMargin
    parse(source) should equal(Seq(Ast("Local", Set("test.inner.Cls"), Set(), Set("java.util.List", "java"))))
  }

  test("should not parse unused imports") {
    val source =
      """
        |import test.inner.Cls;
        |
        |class Local {
        |};
      """.stripMargin
    parse(source) should equal(Seq(Ast("Local", Set(), Set(), Set())))
  }

  test("should not parse static imports") {
    val source =
      """
        |import static alfa.beta.Ceta.method;
        |
        |class Local {
        |};
      """.stripMargin
    parse(source) should equal(Seq(Ast("Local", Set(), Set(), Set())))
  }

  test("should parse * imports") {
    val source =
      """
        |import alfa.beta.*;
        |import alfa.beta.gamma.Delta;
        |
        |class Local {
        |};
      """.stripMargin
    parse(source) should equal(Seq(Ast("Local", Set(), Set("alfa.beta"), Set())))
  }

  test("should yield empty sequence when no classes in source") {
    val source =
      """
        |package test;
        |
        |import alfa.beta.Gamma;
      """.stripMargin
    parse(source) should be('empty)
  }

  private def parse(source: String): Seq[Ast] =
    parser(new ByteArrayInputStream(source.getBytes("utf-8")))
}
