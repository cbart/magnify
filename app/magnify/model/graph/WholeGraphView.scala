package magnify.model.graph

import com.tinkerpop.blueprints.{Edge, Vertex}
import scala.collection.JavaConversions._

/**
 * @author Cezary Bartoszuk (cezary@codilime.com)
 */
final class WholeGraphView (graph: Graph) extends GraphView {

  override def vertices: Iterable[Vertex] =
    graph.vertices.toList

  override def edges: Iterable[Edge] =
    graph.edges.toList
}
