package magnify.features

import java.io._

import scala.collection.mutable
import scala.collection.JavaConversions._
import scala.io.Source
import scala.util.matching.Regex

import com.tinkerpop.blueprints.{Edge, Vertex}
import com.tinkerpop.blueprints.oupls.jung.GraphJung
import com.tinkerpop.gremlin.java.GremlinPipeline
import edu.uci.ics.jung.algorithms.scoring.PageRank
import magnify.model._
import magnify.model.graph.Graph

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[features] final class GraphSources (parse: Parser, imports: Imports) extends Sources {
  private val graphs = mutable.Map[String, Graph]()
  private val importedGraphs = mutable.Map[String, Json]()

  override def add(name: String, file: Archive) {
    val graph = Graph.tinker
    process(graph, classesFrom(file))
    graphs += name -> graph
  }

  override def add(name: String, graph: Json) {
    importedGraphs += name -> graph
  }

  private def classesFrom(file: Archive): Seq[(Ast, String)] = file.extract {
    (name, content) =>
      if (isJavaFile(name) ) {
        val stringContent = inputStreamToString(content)
        for (ast <- parse(new ByteArrayInputStream(stringContent.getBytes("UTF-8")))) yield (ast, stringContent)
      } else {
        Seq()
      }
  }

  private def inputStreamToString(is: InputStream) = {
    val rd: BufferedReader = new BufferedReader(new InputStreamReader(is, "UTF-8"))
    val builder = new StringBuilder()
    try {
      var line = rd.readLine
      while (line != null) {
        builder.append(line + "\n")
        line = rd.readLine
      }
    } finally {
      rd.close()
    }
    builder.toString()
  }

  private def isJavaFile(name: String): Boolean =
    name.endsWith(".java") && !name.endsWith("Test.java")

  private def process(graph: Graph, classes: Iterable[(Ast, String)]) {
    addClasses(graph, classes)
    addImports(graph, classes.map(_._1))
    addPackages(graph)
    computeLinesOfCode(graph)
  }

  private def addClasses(graph: Graph, classes: Iterable[(Ast, String)]) {
    for ((ast, source) <- classes) {
      val vertex = graph.addVertex
      vertex.setProperty("kind", "class")
      vertex.setProperty("name", ast.className)
      vertex.setProperty("source-code", source)
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
    addPageRank(graph)
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

  private def addPageRank(graph: Graph) {
    val pageRank = new PageRank[Vertex, Edge](new GraphJung(graph.blueprintsGraph), 0.15)
    pageRank.evaluate()
    for (vertex <- graph.blueprintsGraph.getVertices) {
      vertex.setProperty("page-rank", pageRank.getVertexScore(vertex).toString)
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
    graphs.keys.toSeq ++ importedGraphs.keys.toSeq

  override def get(name: String): Option[Graph] =
    graphs.get(name)

  override def getJson(name: String) =
    importedGraphs.get(name)

  private def computeLinesOfCode(graph: Graph) {
    graph
      .vertices
      .has("kind", "class")
      .toList foreach {
      case v: Vertex =>
        val linesOfCode = v.getProperty("source-code").toString.count(_ == '\n')
        v.setProperty("metric--lines-of-code", linesOfCode)
    }
    graph
      .vertices
      .has("kind", "package").toList foreach { case pkg: Vertex =>
      val elems = graph.vertices.has("name", pkg.getProperty("name"))
        .in("in-package")
        .has("kind", "class")
        .property("metric--lines-of-code")
        .toList.toSeq.asInstanceOf[mutable.Seq[Int]]
      val avg = elems.sum.toDouble / elems.size.toDouble
      pkg.setProperty("metric--lines-of-code", avg)
    }
  }

  private object CsvCall extends Regex("""([^;]+);([^;]+);(\d+)""", "from", "to", "count")

  private object PackageFromCall extends Regex("""(.* |^)([^ ]*)\.[^.]+\.[^.(]+\(.*""")

  def addRuntime(name: String, file: File) {
    for (graph <- get(name)) {
      val runtime = for {
        CsvCall(from, to, count) <- Source.fromFile(file).getLines().toSeq
        PackageFromCall(_, fromPackage) = from
        PackageFromCall(_, toPackage) = to
      } yield (fromPackage, toPackage, count.toInt)
      val calls = runtime.groupBy {case (a, b, _) => (a, b)}.mapValues(s => s.map(_._3).sum)
      for {
        ((fromPackage, toPackage), count) <- calls
        from <- graph.vertices.has("kind", "package").has("name", fromPackage).toList
        to <- graph.vertices.has("kind", "package").has("name", toPackage).toList
      } {
        val e = graph.addEdge(from.asInstanceOf[Vertex], "calls", to.asInstanceOf[Vertex])
        e.setProperty("count", count.toString)
      }
    }
  }
}
