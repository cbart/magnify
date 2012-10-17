package magnify.web.api.view

import akka.actor._
import com.google.inject.{Provider, Inject}

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[view] final class View @Inject() (actorSystem: ActorSystem) extends Provider[ActorRef] {
  override def get: ActorRef = actorSystem.actorOf(
    props = Props(
      new Actor {
        override protected def receive = {
          case model: ViewModel => model.respond()
        }
      }
    ),
    name = "view")
}
