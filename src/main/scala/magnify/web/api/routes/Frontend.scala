package magnify.web.api.routes

import cc.spray._
import cc.spray.http.MediaTypes._
import cc.spray.http.MediaType
import cc.spray.http.HttpMethods.GET
import com.google.inject.{Provider, Inject}
import akka.actor.ActorSystem

/**
 * Web Browser frontend routes.
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[routes] final class Frontend @Inject() (actorSystem: ActorSystem) extends Provider[Route] {
  val directives = Directives(actorSystem)

  import directives._


  override def get: Route =
    (pathPrefix("frontend") & method(GET)) {
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