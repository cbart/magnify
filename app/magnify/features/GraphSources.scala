package magnify.features

import com.tinkerpop.blueprints.impls.tg.TinkerGraph
import com.tinkerpop.blueprints.{Vertex, Graph}
import com.tinkerpop.gremlin.java.GremlinPipeline
import magnify.model.{Archive, Ast}
import scala.collection.JavaConversions._
import scala.collection.mutable

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[features] final class GraphSources (parse: Parser, imports: Imports) extends Sources {
  private val graphs = mutable.Map[String, Graph]()

  override def add(name: String, file: Archive) {
    val graph = new TinkerGraph
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
      val vertex = graph.addVertex(null)
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
      graph.addEdge(null, outVertex, inVertex, "imports")
    }
  }

  private def classesNamed(graph: Graph, name: String): Iterable[Vertex] =
    new GremlinPipeline[java.lang.Iterable[Vertex], java.lang.Iterable[Vertex]]()
        .start(graph.getVertices)
        .has("kind", "class")
        .has("name", name)
        .toList
        .asInstanceOf[java.util.List[Vertex]]
}
