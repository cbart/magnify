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
import play.api.Logger

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[features] final class GraphSources (parse: Parser, imports: Imports) extends Sources {

  private val logger = Logger(classOf[GraphSources].getSimpleName)

  private val graphs = mutable.Map[String, Graph]()
  private val importedGraphs = mutable.Map[String, Json]()

  class ClassExtractor {
    var currentClasses = Map[String, Set[String]]() // file name -> class name
    var changedFiles = Set[String]()

    def newCommit(diff: ChangeDescription): Unit = {
      currentClasses = currentClasses.filterKeys((fileName) => !diff.removedFiles.contains(diff))
      changedFiles = diff.changedFiles
    }

    def shouldParse(fileName: String): Boolean =
      changedFiles.contains(fileName) || !currentClasses.containsKey(fileName)

    def parsedFile(fileName: String, classes: Seq[ParsedFile]) =
      currentClasses = currentClasses.updated(fileName, classes.map(_.ast.className).toSet)

    def classes: Set[String] = currentClasses.values.toSet.flatten
  }

  override def add(name: String, vArchive: VersionedArchive) {
    val graph = Graph.tinker
    val classExtractor = new ClassExtractor()
    logger.info("Revision analysis starts: " + name + " : " + System.nanoTime())
    vArchive.extract { (archive, diff) =>
      logger.debug("processing commit @ " + name + " : " + diff.revision + " : " + System.nanoTime())
      classExtractor.newCommit(diff)
      val classes = classesFrom(archive, classExtractor)
      processRevision(graph, diff, classes)
      graph.commitVersion(diff, classExtractor.classes)
      Seq() // for monoid to work
    }
    logger.info("Revision analysis finished: " + name + " : " + System.nanoTime())
    logger.info("PageRank starts: " + name + " : " + System.nanoTime())
    // addPageRank(graph) // TODO(biczel): Make it work on single revision layer.
    logger.info("PageRank finished: " + name + " : " + System.nanoTime())
    logger.info("Add package Imports starts: " + name + " : " + System.nanoTime())
    addPackageImports(graph)
    logger.info("Add package Imports finished: " + name + " : " + System.nanoTime())
    logger.info("Compute LOC starts: " + name + " : " + System.nanoTime())
    computeLinesOfCode(graph, vArchive)
    logger.info("Compute LOC finished: " + name + " : " + System.nanoTime())
    graphs += name -> graph
  }

  override def add(name: String, graph: Json) {
    importedGraphs += name -> graph
  }

  private def classesFrom(file: Archive, classExtractor: ClassExtractor): Seq[ParsedFile] =
    file.extract { (fileName, oFileId, content) =>
      if (isJavaFile(fileName) && classExtractor.shouldParse(fileName)) {
        val stringContent = inputStreamToString(content())
        val parsedFiles = for (ast <- parse(new ByteArrayInputStream(stringContent.getBytes("UTF-8")))) yield (
            ParsedFile(ast, stringContent, fileName, oFileId))
        classExtractor.parsedFile(fileName, parsedFiles)
        parsedFiles
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
      val (cls, commitEdge) = graph.addVertex(
        "class", parsedFile.ast.className, Seq("file-name" -> parsedFile.fileName).toMap)
      commitEdge.map(changeDescription.setProperties(_))
      if (parsedFile.oFileId.isDefined) {
        cls.setProperty("object-id", parsedFile.oFileId.get)
      } else {
        cls.setProperty("source-code", parsedFile.content)
      }
      val linesOfCode = parsedFile.content.count(_ == '\n')
      cls.setProperty("metric--lines-of-code", linesOfCode)
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
      val (pkg, commitEdge) = graph.addVertex("package", pkgName)
      commitEdge.map(changeDescription.setProperties(_))
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

  private def computeLinesOfCode(graph: Graph, vArchive: VersionedArchive) {
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

private case class ParsedFile(ast: Ast, content: String, fileName: String, oFileId: Option[String])
