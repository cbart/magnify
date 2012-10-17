package magnify.features.project.graph

import com.tinkerpop.blueprints.Graph

/**
 * Message that results in retrieving graph for project named {{{projectName}}} and passing
 * the graph to given {{{continuation}}}.
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
final case class GetProjectGraph (projectName: String, continuation: Graph => Unit)
