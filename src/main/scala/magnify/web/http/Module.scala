package magnify.web.http

import cc.spray.io.IoWorker
import akka.actor.Props
import cc.spray.can.server.HttpServer
import cc.spray.io.pipelines.MessageHandlerDispatch
import cc.spray.SprayCanRootService
import magnify.web.api
import magnify.core

/**
 * Web server actors.
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
trait Module {
  this: api.Module with core.Module =>

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
