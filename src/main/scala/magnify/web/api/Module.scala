package magnify.web.api

import akka.actor.{ActorRef, Props}
import cc.spray._
import http.{StatusCodes, HttpResponse}
import magnify.core
import magnify.web.api.routes._

trait Module {
  this: core.Module =>

  val routes =
    new Frontend().route ::
    new Control().route ::
    new Data().route :: Nil

  def rejectionHandler: PartialFunction[List[Rejection], HttpResponse] = {
    case _ => HttpResponse(StatusCodes.BadRequest)
  }

  val svc: Route => ActorRef = { route =>
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
