package magnify.web.http

import com.google.inject.{Provider, Inject}
import akka.actor.{Props, ActorRef, ActorSystem}
import com.google.inject.name.Named
import cc.spray.SprayCanRootService

/**
 *
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[http] final class SprayCanRootServiceProvider @Inject()
    (actorSystem: ActorSystem, @Named("root-service") rootService: ActorRef)
    extends Provider[ActorRef] {
  override def get: ActorRef = actorSystem.actorOf(
    props = Props(new SprayCanRootService(rootService)),
    name = "spray-can-root-service"
  )
}
