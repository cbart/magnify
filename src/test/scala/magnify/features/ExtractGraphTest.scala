package magnify.features

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.io.{ByteArrayInputStream, InputStream}
import scala.io.Source
import magnify.model.graph.Graph
import org.scalatest.matchers.ShouldMatchers

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
@RunWith(classOf[JUnitRunner])
final class ExtractGraphTest extends FunSuite with ShouldMatchers {
  private val mockSources = Seq("mock", "Java", "sources")

  private def readString: InputStream => Seq[String] =
    stream => Source
      .fromInputStream(stream, "utf-8")
      .getLines
      .mkString("\n") :: Nil

  private val toInputStream: String => InputStream =
    string => new ByteArrayInputStream(string.getBytes("utf-8"))

  private def makeGraph(identifiers: Seq[String]): Graph =
    Graph(identifiers.map(id => id -> Seq(id)).toMap)

  private object mockReader extends ExtractGraph.Reader {
    def apply[T](f: InputStream => Seq[T]) = mockSources flatMap (toInputStream andThen f)
  }

  val extract = new ExtractGraph(readString, makeGraph)

  test("should create graph with same sequence as mock sources") {
    val Graph(extracted) = extract(mockReader)
    extracted.keys.toSet should equal(mockSources.toSet)
  }
}
