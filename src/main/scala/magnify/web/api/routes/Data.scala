package magnify.web.api.routes

import magnify.web.api.controllers.DataController

import akka.actor.ActorSystem
import spray.routing._
import spray.routing.Directives._

/**
 * Data manipulation REST routes.
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[routes] final class Data (system: ActorSystem, controller: DataController)
    extends (() => Route) {

  override def apply: Route = {
    (post & path("data" / "projects")) {
      formFields(('projectName.as[String], 'Filedata.as[Array[Byte]])) {
        controller.uploadZipSrc _
      }
    }
  }
}