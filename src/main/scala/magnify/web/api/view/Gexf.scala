package magnify.web.api.view

import magnify.model.graph.Graph

/**
 * [[com.tinkerpop.blueprints.Graph]] pimp that adds GEXF serialization.
 */
class Gexf (graph: Graph) {
  def toXml =
    <gexf xmlns="http://www.gexf.net/1.2draft" version="1.2">
      <meta lastmodifieddate="2009-03-20">
        <creator>Magnify</creator>
        <description>Graph</description>
      </meta>
      <graph mode="static" defaultedgetype="directed">
        <nodes>{ nodes }</nodes>
        <edges>{ edges }</edges>
      </graph>
    </gexf>

  def nodes: Iterable[xml.Node] =
    graph.imports.keys.map { v =>
      <node id={v}/>
    }

  def edges: Iterable[xml.Node] =
    for {
      (fromNode, successors) <- graph.imports.toSeq
      toNode <- successors
    } yield <edge id={"%s_%s".format(fromNode, toNode)} source={fromNode} target={toNode}/>
}
