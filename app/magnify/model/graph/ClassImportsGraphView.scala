package magnify.model.graph

import scala.collection.JavaConversions._

import com.tinkerpop.blueprints.{Edge, Vertex}
import com.tinkerpop.gremlin.pipes.filter.{LabelFilterPipe, PropertyFilterPipe}
import com.tinkerpop.pipes.filter.FilterPipe.Filter

final class ClassImportsGraphView(graph: Graph) extends GraphView {

  override def vertices: Iterable[Vertex] =
    graph.revVertices()
        .add(classes)
        .toList

  private val classes =
    new PropertyFilterPipe[Vertex, String]("kind", "class", Filter.EQUAL)

  override def edges: Iterable[Edge] =
    graph.edges()
        .add(imports)
        .toList

  private val imports =
    new LabelFilterPipe("imports", Filter.EQUAL)
}
