package magnify.web.api.routes

import akka.actor._
import spray.routing._

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[routes] final class RoutingActor (routes: Iterable[Route])
    extends Actor with HttpServiceActor {
  override def receive = runRoute(routes.reduce(_ ~ _))
}

