package magnify.web.api.routes

import akka.actor.ActorSystem
import cc.spray._
import cc.spray.http.MediaTypes._
import cc.spray.http.MediaType

/**
 * Web Browser frontend routes.
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[routes] final class Frontend (system: ActorSystem) extends (() => Route) {
  val directives = Directives(system)

  import directives._

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