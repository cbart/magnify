package magnify.model

import scalaz.Monoid

trait VersionedArchive  {
  def extract[A : Monoid](f: (Archive, ChangeDescription) => A): A

  def getContent(objectId: String): String = ???
}
