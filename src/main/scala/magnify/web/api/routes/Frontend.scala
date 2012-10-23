package magnify.web.api.routes

import akka.actor.ActorSystem
import spray.routing._
import spray.http._

/**
 * Web Browser frontend routes.
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[routes] final class Frontend (implicit system: ActorSystem) extends (() => Route) {
  import Directives._
  import MediaTypes._

  override def apply: Route =
    (pathPrefix("frontend") & get) {
      static("html") ~
      static("js", mediaType = `application/javascript`) ~
      static("css", mediaType = `text/css`) ~
      static("img", mediaType = `image/png`)
    }

  /**
   * Returns a route mapping {{{ pathElement }}} url prefix to {{{ pathElement }}} resource path.
   * Example above.
   */
  private def static(pathElement: String, mediaType: MediaType = `text/html`) = {
    pathPrefix(pathElement) {
      respondWithMediaType(mediaType) {
        getFromResourceDirectory(pathElement)
      }
    }
  }
}