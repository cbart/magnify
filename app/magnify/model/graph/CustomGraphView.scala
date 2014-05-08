package magnify.model.graph

import com.tinkerpop.blueprints.{Edge, Vertex}
import scala.collection.JavaConversions._
import com.tinkerpop.gremlin.pipes.filter.{LabelFilterPipe, PropertyFilterPipe}
import com.tinkerpop.pipes.filter.FilterPipe.Filter
import com.tinkerpop.pipes.filter.OrFilterPipe

/**
 * @author Cezary Bartoszuk (cezary@codilime.com)
 */
final class CustomGraphView (graph: Graph, revision: Option[String]) extends GraphView {

  override def vertices: Iterable[Vertex] =
    graph.revVertices(revision)
        .add(packages)
        .toList

  private val packages =
    new PropertyFilterPipe[Vertex, String]("kind", "package", Filter.EQUAL)

  override def edges: Iterable[Edge] =
    graph.edges(revision)
        .add(imports)
        .toList

  private val imports =
    new OrFilterPipe[Edge](
      new LabelFilterPipe("package-imports", Filter.EQUAL),
      new LabelFilterPipe("in-package", Filter.EQUAL),
      new LabelFilterPipe("calls", Filter.EQUAL))
}
