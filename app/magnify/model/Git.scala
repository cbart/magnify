package magnify.model

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File, InputStream}

import scala.annotation.tailrec
import scala.collection.JavaConversions._

import org.eclipse.jgit.diff.{DiffFormatter, RawTextComparator}
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.{RevCommit, RevWalk}
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.util.io.DisabledOutputStream
import play.api.Logger
import scalaz.Monoid

/**
 * @author Tomasz Biczel (tomasz@biczel.com)
 */
private[this] final class Git(repo: Repository, branch: String) extends VersionedArchive {

  private val logger = Logger(classOf[Git].getSimpleName)

  logger.info("Having repo: " + repo.getDirectory)
  logger.debug("Getting head of branch: " + branch)
  val revWalk = new RevWalk(repo)
  val head = revWalk.parseCommit(repo.getRef("refs/heads/" + branch).getObjectId)
  logger.debug("Start Commit: " + head)
  revWalk.markStart(head)
  val reader = repo.newObjectReader()
  val df = new DiffFormatter(DisabledOutputStream.INSTANCE)
  df.setRepository(repo)
  df.setDiffComparator(RawTextComparator.DEFAULT)
  df.setDetectRenames(true)

  override def extract[A: Monoid](f: (Archive, Option[ChangeDescription]) => A): A = {
    val monoid = implicitly[Monoid[A]]
    fold(monoid.zero, revWalk, None, (acc: A, archive: Archive, changeDesc: Option[ChangeDescription]) =>
      monoid.append(acc, f(archive, changeDesc)))
  }

  @tailrec
  private def fold[A](
      acc: A, revWalk: RevWalk, parent: Option[RevCommit],
      transform: (A, Archive, Option[ChangeDescription]) => A): A = {
    Option(revWalk.next()) match {
      case Some(revCommit) => {
        logger.debug("Processing commit: " + revCommit)
        val changeDesc = parent.map { parentCommit =>
          val diffs = df.scan(revCommit.getTree, parentCommit.getTree).toSeq
          ChangeDescription(
            reader.abbreviate(revCommit.getId).name,
            revCommit.getFullMessage,
            revCommit.getAuthorIdent.toExternalString,
            revCommit.getCommitterIdent.toExternalString,
            revCommit.getCommitTime,
            diffs.map((diff) =>
              (Option(diff.getOldPath).filter(_ ne "/dev/null") -> Option(diff.getNewPath).filter(_ ne "/dev/null"))
            ).toMap)
        }
        fold(transform(acc, new GitCommit(repo, revCommit, parent), changeDesc), revWalk, Some(revCommit), transform)
      }
      case None => acc
    }
  }
}

private[this] final class GitCommit(
    repo: Repository,
    commit: RevCommit,
    parent: Option[RevCommit])
  extends Archive {

  private val logger = Logger(classOf[GitCommit].getSimpleName)
  
  override def extract[A: Monoid](f: (String, InputStream) => A): A = {
    val tree = commit.getTree()
    logger.debug("Having tree: " + tree)
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

  def apply(path: String, branch: Option[String] = None): VersionedArchive = new Git(
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
