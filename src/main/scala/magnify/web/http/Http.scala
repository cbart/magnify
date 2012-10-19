package magnify.web.http

import akka.actor._
import cc.spray.io.IoWorker
import com.google.inject._
import com.google.inject.name.{Named, Names}
import cc.spray.SprayCanRootService
import cc.spray.can.server.HttpServer
import cc.spray.io.pipelines.MessageHandlerDispatch

/**
 * Web server actors.
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
final class Http extends PrivateModule {
  protected override def configure() {
    requireBinding(classOf[ActorSystem])
    requireBinding(Key.get(classOf[ActorRef], Names.named("root-service")))
  }

  @Provides
  @Named("spray-can-root-service")
  private def sprayCanRootService(system: ActorSystem,
      @Named("root-service") rootService: ActorRef): ActorRef = system.actorOf(
    props = Props(new SprayCanRootService(rootService)),
    name = "spray-can-root-service"
  )

  @Provides
  private def ioWorker(system: ActorSystem): IoWorker = {
    val ioWorker = new IoWorker(system).start()
    system.registerOnTermination {
      ioWorker.stop()
    }
    ioWorker
  }

  @Exposed
  @Provides
  @Named("http-server")
  private def httpServer(system: ActorSystem, ioWorker: IoWorker,
      @Named("spray-can-root-service") sprayCanRootService: ActorRef): ActorRef = system.actorOf(
    props = Props(new HttpServer(ioWorker,
      MessageHandlerDispatch.SingletonHandler(sprayCanRootService))),
    name = "http-server"
  )
}
