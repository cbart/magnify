package magnify.model

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File, InputStream}

import scala.annotation.tailrec

import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.treewalk.TreeWalk
import scalaz.Monoid

/**
 * @author Tomasz Biczel (tomasz@biczel.com)
 */
final class Git(path: String) extends Archive {

  private val repo: Repository = {
    val builder = new FileRepositoryBuilder()
    builder.setGitDir(new File(path, ".git"))
        .readEnvironment() // scan environment GIT_* variables
        .findGitDir() // scan up the file system tree
        .build()
  }
  println("Having repo: " + repo.getDirectory)
  val revWalk = new RevWalk(repo)
  val commit = revWalk.parseCommit(repo.getRef("refs/heads/master").getObjectId)
  val tree = commit.getTree()
  println("Having tree: " + tree)

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
