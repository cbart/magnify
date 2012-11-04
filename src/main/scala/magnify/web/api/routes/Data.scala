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
    pathPrefix("data" / "projects") {
      (post & path(PathEnd)) {
        formFields(('projectName.as[String], 'Filedata.as[Array[Byte]])) {
          controller.uploadZipSrc _
        }
      } ~
      (get & path(PathElement / "head" / "whole.gexf")) {
        controller.showGraph _
      }
    }
  }
}