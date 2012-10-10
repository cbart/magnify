package magnify.web.http

import magnify.core.Core
import magnify.web.api.routes.Routes

import akka.actor.Props
import cc.spray.SprayCanRootService
import cc.spray.can.server.HttpServer
import cc.spray.io.IoWorker
import cc.spray.io.pipelines.MessageHandlerDispatch

/**
 * Web server actors.
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
trait Http {
  this: Routes with Core =>

  // Low-level network IO.
  val ioWorker = new IoWorker(actorSystem).start()

  val sprayCanRootService = actorSystem.actorOf(
    props = Props(new SprayCanRootService(rootService)),
    name = "spray-can-root-service"
  )

  // Create and start the spray-can HttpServer.
  val sprayCanServer = actorSystem.actorOf(
    Props(new HttpServer(ioWorker,
      MessageHandlerDispatch.SingletonHandler(sprayCanRootService))),
    name = "http-server"
  )

  // At which port the HTTP server listens at.
  sprayCanServer ! HttpServer.Bind("localhost", 8080)

  // Hook IoWorker shutdown when actor system goes down.
  actorSystem.registerOnTermination {
    ioWorker.stop()
  }
}
