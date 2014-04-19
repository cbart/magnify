package magnify.model

import scalaz.Monoid

trait VersionedArchive  {
  def extract[A : Monoid](f: (Archive, Option[ChangeDescription]) => A): A
}
