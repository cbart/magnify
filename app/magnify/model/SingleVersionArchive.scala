package magnify.model

import scalaz.Monoid

class SingleVersionArchive(archive: Archive) extends VersionedArchive {

  val changedFiles = archive.extract((fileName, oObjectId, content) => Set(fileName))
  val diff = ChangeDescription("", "", "", "", 0, changedFiles, Set[String]())

  override def extract[A: Monoid](f: (Archive, ChangeDescription) => A): A = f(archive, diff)
}
