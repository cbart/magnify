package magnify.web.api.routes

import akka.actor.ActorSystem
import spray.http.MediaType
import spray.http.MediaTypes._
import spray.routing.Directives._
import spray.routing.Route

/**
 * Web Browser frontend routes.
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[routes] final class Frontend (implicit system: ActorSystem) extends (() => Route) {

  override def apply: Route =
    get {
      pathPrefix("frontend") {
        static("html") ~
        static("js", mediaType = `application/javascript`) ~
        static("css", mediaType = `text/css`) ~
        static("img", mediaType = `image/png`)
      }
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