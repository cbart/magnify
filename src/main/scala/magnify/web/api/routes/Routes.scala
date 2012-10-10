package magnify.web.api.routes

import magnify.core.Core

import akka.actor.{ActorRef, Props}
import cc.spray._
import cc.spray.http.{StatusCodes, HttpResponse}

trait Routes extends Frontend with Data with Control {
  this: Core =>

  private def routes =
    frontendDirectives.route ::
    dataDirectives.route ::
    controlDirectives.route :: Nil

  private def rejectionHandler: PartialFunction[List[Rejection], HttpResponse] = {
    case _ => HttpResponse(StatusCodes.BadRequest)
  }

  private val svc: Route => ActorRef = { route =>
    actorSystem.actorOf(Props(new HttpService(route, rejectionHandler)))
  }

  val rootService = actorSystem.actorOf(
    props = Props(new RootService(
      svc(routes.head),
      routes.tail.map(svc): _*
    )),
    name = "root-service"
  )
}