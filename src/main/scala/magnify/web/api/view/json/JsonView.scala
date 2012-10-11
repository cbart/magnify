package magnify.web.api.view
package json

import akka.actor._
import com.google.inject.{Inject, Provider}

/**
 *
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[view] final class JsonView @Inject() (actorSystem: ActorSystem) extends Provider[ActorRef] {
  override def get: ActorRef = actorSystem.actorOf(
    props = Props(
      new Actor {
        override protected def receive = {
          case model: JsonModel[_] => model.respond()
        }
      }
    ),
    name = "json-view")
}
