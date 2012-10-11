package magnify.web.api.routes

import akka.actor.{Props, ActorRef, ActorSystem}
import cc.spray._
import cc.spray.http.{StatusCodes, HttpResponse}
import com.google.inject.{Provider, Inject}

import scala.collection.JavaConversions._
import com.google.inject.name.Named

/**
 *
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[routes] final class RootServiceProvider @Inject()
    (actorSystem: ActorSystem, @Named("routes") routes: java.util.Set[Route])
    extends Provider[ActorRef] {
  override def get: ActorRef = {
    val linearRoutes = routes.toSeq
    actorSystem.actorOf(
      props = Props(new RootService(
        svc(linearRoutes.head),
        linearRoutes.tail.map(svc): _*
      )),
      name = "root-service")
  }

  private val svc: Route => ActorRef = { route =>
    actorSystem.actorOf(Props(new HttpService(route, rejectionHandler)))
  }

  private val rejectionHandler: PartialFunction[List[Rejection], HttpResponse] = {
    case _ => HttpResponse(StatusCodes.BadRequest)
  }
}
