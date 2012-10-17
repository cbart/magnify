package magnify.services.project.repository

import com.tinkerpop.blueprints.Graph

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
final case class GetGraph (projectName: String, continuation: Option[Graph] => Unit)
