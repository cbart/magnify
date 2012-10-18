package magnify.web.api.routes

import akka.actor.{Props, ActorRef, ActorSystem}
import cc.spray._
import cc.spray.http.{StatusCodes, HttpResponse}

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[routes] final class RootServiceProvider (system: ActorSystem, routes: Iterable[Route])
    extends (() => ActorRef) {
  override def apply: ActorRef = {
    val linearRoutes = routes.toSeq
    system.actorOf(
      props = Props(new RootService(
        svc(linearRoutes.head),
        linearRoutes.tail.map(svc): _*
      )),
      name = "root-service")
  }

  private val svc: Route => ActorRef = { route =>
    system.actorOf(Props(new HttpService(route, rejectionHandler)))
  }

  private val rejectionHandler: PartialFunction[List[Rejection], HttpResponse] = {
    case _ => HttpResponse(StatusCodes.BadRequest)
  }
}
