package magnify.model

import scalaz.Monoid

class SingleVersionArchive(archive: Archive) extends VersionedArchive {
  override def extract[A: Monoid](f: (Archive, Option[ChangeDescription]) => A): A = f(archive, None)
}
