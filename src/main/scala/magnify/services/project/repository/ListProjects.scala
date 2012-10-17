package magnify.services.project.repository

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
final case class ListProjects (continuation: Seq[String] => Unit)
