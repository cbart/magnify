package magnify.features

import java.io._

import scala.annotation.tailrec
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
import play.api.Logger

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[features] final class GraphSources (parse: Parser, imports: Imports) extends Sources {

  private val logger = Logger(classOf[GraphSources].getSimpleName)

  private val graphs = mutable.Map[String, Graph]()
  private val importedGraphs = mutable.Map[String, Json]()

  override def add(name: String, vArchive: VersionedArchive) {
    val graph = Graph.tinker
    vArchive.extract { (archive, diff) =>
      removeClassesAndPackagesAddedInNextRevision(graph, diff.removedFiles)
      val classes = classesFrom(archive)
      val changedClasses = classes.filter((parsedFile) => diff.changedFiles.contains(parsedFile.fileName))
      processRevision(graph, diff, changedClasses)
      graph.commitVersion(diff)
      Seq() // for monoid to work
    }
    addPageRank(graph) // TODO(biczel): Make it work on single revision layer.
    addPackageImports(graph)
    computeLinesOfCode(graph)
    graphs += name -> graph
  }

  override def add(name: String, graph: Json) {
    importedGraphs += name -> graph
  }

  private def classesFrom(file: Archive): Seq[ParsedFile] = file.extract {
    (fileName, content) =>
      if (isJavaFile(fileName) ) {
        val stringContent = inputStreamToString(content)
        for (ast <- parse(new ByteArrayInputStream(stringContent.getBytes("UTF-8")))) yield (ParsedFile(
            ast, stringContent, fileName))
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

  private def removeClassesAndPackagesAddedInNextRevision(graph: Graph, removedFiles: Set[String]) = {
    for (fileName <- removedFiles) {
      val classes = graph.currentVertices.has("file-name", fileName).has("kind", "class").toList
      for (cls <- classes) {
        graph.removeFromCurrent("class", cls.getProperty("name"))
      }
    }
    removeEmptyPackages(graph)
  }

  @tailrec
  private def removeEmptyPackages(graph: Graph): Unit = {
    val packages = graph.currentVertices.has("kind", "package").toList
    val pkgWithClasses = packages.map((pkg) => (pkg, new GremlinPipeline()
        .start(pkg)
        .in("in-package")
        .filter(graph.currentVerticesFilter)
        .toList))
    val emptyPkgs = pkgWithClasses.filter(_._2.isEmpty).map(_._1)
    for (pkg <- emptyPkgs) {
      graph.removeFromCurrent("package", pkg.getProperty("name"))
    }
    if (!emptyPkgs.isEmpty) { removeEmptyPackages(graph) }
  }

  private def processRevision(
      graph: Graph,
      changeDescription: ChangeDescription,
      classes: Iterable[ParsedFile]) {
    val clsVertices = classes.map(addClasses(graph, changeDescription))
    addImports(graph, classes.map(_.ast))
    addPackages(graph, changeDescription, clsVertices)
  }

  private def addClasses(graph: Graph, changeDescription: ChangeDescription): (ParsedFile => Vertex) = {
    parsedFile =>
      val (cls, newerClass) = graph.addVertex("class", parsedFile.ast.className)
      newerClass match {
        case (Some(newerClass)) => changeDescription.setProperties(graph.addEdge(cls, "commit", newerClass))
        case _ => ()
      }
      cls.setProperty("source-code", parsedFile.content)
      cls.setProperty("file-name", parsedFile.fileName)
      cls
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

  private def addPackages(graph: Graph, changeDescription: ChangeDescription, classes: Iterable[Vertex]) {
    val packageNames = packagesFrom(classes)
    val packageByName = addPackageVertices(graph, changeDescription, packageNames)
    addPackageEdges(graph, packageByName)
    addClassPackageEdges(graph, classes, packageByName)
  }

  private def packagesFrom(classes: Iterable[Vertex]): Set[String] =
    (for {
      cls <- classes
      clsName = name(cls)
      pkgName <- clsName.split('.').inits.toList.tail.map(_.mkString("."))
    } yield pkgName).toSet

  private def addPackageVertices(
      graph: Graph, changeDescription: ChangeDescription, packageNames: Set[String]): Map[String, Vertex] =
    (for (pkgName <- packageNames) yield {
      val (pkg, newerPkg) = graph.addVertex("package", pkgName)
      newerPkg match {
        case Some(newerPkg) => changeDescription.setProperties(graph.addEdge(pkg, "commit", newerPkg))
        case _ => ()
      }
      pkgName -> pkg
    }).toMap

  private def addPackageEdges(graph: Graph, packageByName: Map[String, Vertex]) {
    for ((name, pkg) <- packageByName; if name.nonEmpty) {
      val outer = packageByName(parentPkgName(name))
      graph.addEdge(pkg, "in-package", outer)
    }
  }

  private def addClassPackageEdges(graph: Graph, classes: Iterable[Vertex], packageByName: Map[String, Vertex]) {
    for (cls <- classes) {
      val pkg = packageByName(parentPkgName(name(cls)))
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

  private def parentPkgName(name: String): String =
    if (name.contains('.')) {
      name.substring(0, name.lastIndexOf('.'))
    } else {
      ""
    }

  private def name(cls: Vertex): String =
    cls.getProperty("name").toString

  private def classesNamed(graph: Graph, name: String): Iterable[Vertex] =
    graph
      .currentVertices
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
      val elems = new GremlinPipeline()
        .start(pkg)
        .in("in-package")
        .has("kind", "class")
        .property("metric--lines-of-code")
        .toList.toSeq.asInstanceOf[mutable.Seq[Int]]
      val avg = Option(elems).filter(_.size > 0).map(_.sum.toDouble / elems.size.toDouble)
      pkg.setProperty("metric--lines-of-code", avg.getOrElse(0))
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
        from <- graph.revVertices().has("kind", "package").has("name", fromPackage).toList
        to <- graph.revVertices().has("kind", "package").has("name", toPackage).toList
      } {
        val e = graph.addEdge(from.asInstanceOf[Vertex], "calls", to.asInstanceOf[Vertex])
        e.setProperty("count", count.toString)
      }
    }
  }
}

private case class ParsedFile(ast: Ast, content: String, fileName: String)
