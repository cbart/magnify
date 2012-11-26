package magnify.model.graph

import com.tinkerpop.blueprints.{Edge, Vertex}

/**
 * @author Cezary Bartoszuk (cezary@codilime.com)
 */
trait GraphView {

  def vertices: Iterable[Vertex]

  def edges: Iterable[Edge]
}
