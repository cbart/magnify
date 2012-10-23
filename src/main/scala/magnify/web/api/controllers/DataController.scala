package magnify.web.api.controllers

import spray.routing.RequestContext

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
trait DataController {
  def uploadSources(context: RequestContext)
}
