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
      static("html" -> "html") ~
      static("js" -> "javascript", mediaType = `application/javascript`) ~
      static("style" -> "css", mediaType = `text/css`)
    }

  /**
   * Returns a route mapping {{{ pathMapping._1 }}} url prefix to {{{ pathMapping._2 }}} resource
   * path. Example above.
   */
  private def static(pathMapping: (String, String), mediaType: MediaType = `text/html`) = {
    val (urlPrefix, resourcesPath) = pathMapping
    pathPrefix(urlPrefix) {
      respondWithMediaType(mediaType) {
        getFromResourceDirectory(resourcesPath)
      }
    }
  }
}