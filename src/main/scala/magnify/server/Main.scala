package magnify.server

import akka.actor.{ActorSystem, Props, actorRef2Scala}
import cc.spray.can.server.HttpServer
import cc.spray.io.IoWorker
import cc.spray.io.pipelines.MessageHandlerDispatch
import cc.spray.HttpService
import cc.spray.SprayCanRootService

object Main extends App {
  val system = ActorSystem("SimpleHttpServer")
  val mainModule = new GraphService {
    implicit def actorSystem = system
  }
  val httpService = system.actorOf(
    props = Props(new HttpService(mainModule.graphService)),
    name = "http-service"
  )
  val rootService = system.actorOf(
    props = Props(new SprayCanRootService(httpService)),
    name = "root-service"
  )

  // every spray-can HttpServer (and HttpClient) needs an IoWorker for low-level network IO
  // (but several servers and/or clients can share one)
  val ioWorker = new IoWorker(system).start()

  // create and start the spray-can HttpServer, telling it that we want requests to be
  // handled by the root service actor
  val sprayCanServer = system.actorOf(
    Props(new HttpServer(ioWorker, MessageHandlerDispatch.SingletonHandler(rootService))),
    name = "http-server"
  )

  // a running HttpServer can be bound, unbound and rebound
  // initially to need to tell it where to bind to
  sprayCanServer ! HttpServer.Bind("localhost", 8080)

  // finally we drop the main thread but hook the shutdown of
  // our IoWorker into the shutdown of the applications ActorSystem
  system.registerOnTermination {
    ioWorker.stop()
  }
}