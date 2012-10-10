package magnify.web.api.routes

import magnify.core.Core

import cc.spray.Directives
import cc.spray.http.MediaTypes._
import cc.spray.http.MediaType

private[routes] trait Frontend {
  this: Core =>

  private[routes] final class FrontendDirectives extends Directives {
    override def actorSystem = Frontend.this.actorSystem

    val route =
      (pathPrefix("frontend") & get) {
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

  private[routes] val frontendDirectives = new FrontendDirectives
}