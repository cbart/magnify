package magnify.web.http

import com.google.inject.{Provider, Inject}
import akka.actor.{Props, ActorRef, ActorSystem}
import com.google.inject.name.Named
import cc.spray.io.IoWorker
import cc.spray.can.server.HttpServer
import cc.spray.io.pipelines.MessageHandlerDispatch

/**
 *
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[http] final class SprayCanProvider @Inject() (actorSystem: ActorSystem,
    ioWorker: IoWorker, @Named("spray-can-root-service") sprayCanRootService: ActorRef)
    extends Provider[ActorRef] {
  override def get: ActorRef = actorSystem.actorOf(
      props = Props(new HttpServer(ioWorker,
        MessageHandlerDispatch.SingletonHandler(sprayCanRootService))),
      name = "http-server"
    )
}
