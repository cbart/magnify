package magnify.features

import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.gremlin.java.GremlinPipeline
import magnify.model.graph.Graph
import magnify.model.{Archive, Ast}
import scala.collection.JavaConversions._
import scala.collection.mutable
import play.api.Logger
import com.tinkerpop.pipes.Pipe
import com.tinkerpop.gremlin.pipes.transform.OutPipe

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
    addPackages(graph)
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

  private def addPackages(graph: Graph) {
    def classes = graph.vertices.has("kind", "class").toList.toIterable.asInstanceOf[Iterable[Vertex]]
    val packageNames = packagesFrom(classes)
    val packageByName = addPackageVertices(graph, packageNames)
    addPackageEdges(graph, packageByName)
    addClassPackageEdges(graph, classes, packageByName)
    addPackageImports(graph)
  }

  private def packagesFrom(classes: Iterable[Vertex]): Set[String] =
    (for {
      cls <- classes
      clsName = name(cls)
      pkgName <- clsName.split('.').inits.toList.tail.map(_.mkString("."))
    } yield pkgName).toSet

  private def addPackageVertices(graph: Graph, packageNames: Set[String]): Map[String, Vertex] =
    (for (pkgName <- packageNames) yield {
      val pkg = graph.addVertex
      pkg.setProperty("kind", "package")
      pkg.setProperty("name", pkgName)
      pkgName -> pkg
    }).toMap

  private def addPackageEdges(graph: Graph, packageByName: Map[String, Vertex]) {
    for ((name, pkg) <- packageByName; if name.nonEmpty) {
      val outer = packageByName(pkgName(name))
      graph.addEdge(pkg, "in-package", outer)
    }
  }

  private def addClassPackageEdges(graph: Graph, classes: Iterable[Vertex], packageByName: Map[String, Vertex]) {
    for (cls <- classes) {
      val pkg = packageByName(pkgName(name(cls)))
      graph.addEdge(cls, "in-package", pkg)
    }
  }

  private def addPackageImports(graph: Graph) {
    for {
      pkg <- graph.vertices
          .has("kind", "class")
          .out("in-package")
          .toList.toSet[Vertex]
      importsPkg <- new GremlinPipeline()
          .start(pkg)
          .in("in-package")
          .out("imports")
          .out("in-package")
          .toList.toSet[Vertex]
    } {
      graph.addEdge(pkg, "package-imports", importsPkg)
    }
  }

  private def pkgName(name: String): String =
    if (name.contains('.')) {
      name.substring(0, name.lastIndexOf('.'))
    } else {
      ""
    }

  private def name(cls: Vertex): String =
    cls.getProperty("name").toString

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
