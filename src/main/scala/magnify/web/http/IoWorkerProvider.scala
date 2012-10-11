package magnify.web.http

import com.google.inject.{Provider, Inject}
import akka.actor.ActorSystem
import cc.spray.io.IoWorker

/**
 *
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[http] final class IoWorkerProvider @Inject() (actorSystem: ActorSystem)
    extends Provider[IoWorker] {
  override def get: IoWorker = {
    val ioWorker = new IoWorker(actorSystem).start()
    actorSystem.registerOnTermination {
      ioWorker.stop()
    }
    ioWorker
  }
}
