package magnify.model

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File, InputStream}

import scala.annotation.tailrec

import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.treewalk.TreeWalk
import play.api.Logger
import scalaz.Monoid

/**
 * @author Tomasz Biczel (tomasz@biczel.com)
 */
private[this] final class Git(repo: Repository, branch: String) extends Archive {

  private val logger = Logger(classOf[Git].getSimpleName)

  logger.info("Having repo: " + repo.getDirectory)
  logger.debug("Getting head of branch: " + branch)
  val revWalk = new RevWalk(repo)
  val commit = revWalk.parseCommit(repo.getRef("refs/heads/" + branch).getObjectId)
  val tree = commit.getTree()
  logger.debug("Having tree: " + tree)

  override def extract[A: Monoid](f: (String, InputStream) => A): A = {
    val treeWalk = new TreeWalk(repo)
    treeWalk.addTree(tree)
    treeWalk.setRecursive(true)
    val monoid = implicitly[Monoid[A]]
    try {
      fold(monoid.zero, treeWalk, (acc: A, name: String, content: InputStream) =>
        monoid.append(acc, f(name, content)))
    } finally {
      repo.close()
    }
  }

  @tailrec
  private def fold[A](acc: A, walk: TreeWalk, transform: (A, String, InputStream) => A): A = {
    if (walk.next()) {
      val loader = repo.open(walk.getObjectId(0))
      val out = new ByteArrayOutputStream()
      loader.copyTo(out)
      fold(transform(acc, walk.getPathString, new ByteArrayInputStream(out.toByteArray)), walk, transform)
    } else {
      acc
    }
  }
}

object Git {

  private val logger = Logger(classOf[Git].getSimpleName)

  def apply(path: String, branch: Option[String] = None): Archive = new Git(
    createRepo(path),
    branch.getOrElse("master"))

  private def createRepo(path: String): Repository = if (isRemote(path)) {
    cloneRemoteRepo(path)
  } else {
    getLocalRepo(if (path.endsWith(".git")) new File(path) else new File(path, ".git"))
  }

  def cloneRemoteRepo(url: String): Repository = {
    val localPath = File.createTempFile("TmpGitRepo", "")
    logger.info("Cloning from " + url + " to " + localPath)
    localPath.delete()
    org.eclipse.jgit.api.Git.cloneRepository()
        .setURI(url)
        .setDirectory(localPath)
        .call()
    getLocalRepo(new File(localPath, ".git"))
  }

  def getLocalRepo(gitFile: File): Repository = {
    new FileRepositoryBuilder()
        .setGitDir(gitFile)
        .readEnvironment() // scan environment GIT_* variables
        .findGitDir() // scan up the file system tree
        .build()
  }

  private def isRemote(path: String): Boolean = path.startsWith("git@") || path.startsWith("http")
}
