package magnify.features

import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.gremlin.java.GremlinPipeline
import magnify.model.graph.Graph
import magnify.model.{Archive, Ast}
import scala.collection.JavaConversions._
import scala.collection.mutable

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[features] final class GraphSources (parse: Parser, imports: Imports) extends Sources {
  private val graphs = mutable.Map[String, Graph]()

  override def add(name: String, file: Archive) {
    val graph = Graph.tinker
    graphs += name -> graph
    process(graph, classesFrom(file))
  }

  private def classesFrom(file: Archive): Seq[Ast] = file.extract {
    (name, content) =>
      if (isJavaFile(name)) {
        parse(content)
      } else {
        Seq()
      }
  }

  private def isJavaFile(name: String): Boolean =
    name.endsWith(".java") && !name.endsWith("Test.java")

  private def process(graph: Graph, classes: Iterable[Ast]) {
    addClasses(graph, classes)
    addImports(graph, classes)
  }

  private def addClasses(graph: Graph, classes: Iterable[Ast]) {
    for (cls <- classes) {
      val vertex = graph.addVertex
      vertex.setProperty("kind", "class")
      vertex.setProperty("name", cls.className)
    }
  }

  private def addImports(graph: Graph, classes: Iterable[Ast]) {
    for {
      (outCls, imported) <- imports.resolve(classes)
      inCls <- imported
    } for {
      inVertex <- classesNamed(graph, inCls)
      outVertex <- classesNamed(graph, outCls)
    } {
      graph.addEdge(outVertex, "imports", inVertex)
    }
  }

  private def classesNamed(graph: Graph, name: String): Iterable[Vertex] =
    graph
      .vertices
      .has("kind", "class")
      .has("name", name)
      .asInstanceOf[GremlinPipeline[Vertex, Vertex]]
      .toList

  override def list: Seq[String] =
    graphs.keys.toSeq

  override def get(name: String): Option[Graph] =
    graphs.get(name)
}
