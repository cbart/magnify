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
    parse(source) should equal(Seq(Ast(Seq(), "magnify.mock.classes.Example")))
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
    parse(source) should equal(Seq(Ast(Seq(), "test.Example1"), Ast(Seq(), "test.Example2")))
  }

  test("should parse imports into fully qualified names") {
    val source =
      """
        |package test;
        |
        |import test.inner.Cls;
        |
        |class Local {
        |};
      """.stripMargin
    parse(source) should equal(Seq(Ast(Seq("test.inner.Cls"), "test.Local")))
  }

  test("should not parse static imports") {
    val source =
      """
        |package test;
        |
        |import static alfa.beta.Ceta.method;
        |import alfa.beta.Gamma;
        |
        |class Local {
        |};
      """.stripMargin
    parse(source) should equal(Seq(Ast(Seq("alfa.beta.Gamma"), "test.Local")))
  }

  test("should not parse * imports") {
    val source =
      """
        |package test;
        |
        |import alfa.beta.*;
        |import alfa.beta.gamma.Delta;
        |
        |class Local {
        |};
      """.stripMargin
    parse(source) should equal(Seq(Ast(Seq("alfa.beta.gamma.Delta"), "test.Local")))
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
