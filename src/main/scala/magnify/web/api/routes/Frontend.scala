package magnify.web.api.routes

import akka.actor.ActorSystem
import cc.spray.Directives
import cc.spray.http.MediaTypes._
import cc.spray.http.MediaType

final class Frontend (override implicit val actorSystem: ActorSystem) extends Directives {
  val route = {
    (pathPrefix("frontend") & get) {
      static("html" -> "html") ~
      static("js" -> "javascript", mediaType = `application/javascript`) ~
      static("style" -> "css", mediaType = `text/css`)
    }
  }

  /**
   * Returns a route mapping {{{ pathMapping._1 }}} url prefix to {{{ pathMapping._2 }}} resource
   * path. Example above.
   */
  def static(pathMapping: (String, String), mediaType: MediaType = `text/html`) = {
    val (urlPrefix, resourcesPath) = pathMapping
    pathPrefix(urlPrefix) {
      respondWithMediaType(mediaType) {
        getFromResourceDirectory(resourcesPath)
      }
    }
  }
}
