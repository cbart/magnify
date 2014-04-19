package magnify.model.graph

import com.tinkerpop.blueprints.{Edge, Vertex}
import com.tinkerpop.gremlin.pipes.filter.{LabelFilterPipe, PropertyFilterPipe}
import com.tinkerpop.pipes.filter.FilterPipe.Filter
import scala.collection.JavaConversions._

/**
 * @author Cezary Bartoszuk (cezary@codilime.com)
 */
final class PackageImportsGraphView(graph: Graph) extends GraphView {

  override def vertices: Iterable[Vertex] =
    graph.headVertices
        .add(packages)
        .toList

  private val packages =
    new PropertyFilterPipe[Vertex, String]("kind", "package", Filter.EQUAL)

  override def edges: Iterable[Edge] =
    graph.edges
        .add(imports)
        .toList

  private val imports =
    new LabelFilterPipe("package-imports", Filter.EQUAL)
}
