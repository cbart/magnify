package magnify.web.http

import akka.actor.{ActorRef, ActorSystem}
import cc.spray.io.IoWorker
import com.google.inject._
import com.google.inject.name.Names

/**
 * Web server actors.
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
final class Http extends AbstractModule {
  protected override def configure() {
    requireBinding(classOf[ActorSystem])
    requireBinding(Key.get(classOf[ActorRef], Names.named("root-service")))
    bind(classOf[IoWorker])
        .toProvider(classOf[IoWorkerProvider])
        .in(Scopes.SINGLETON)
    bind(classOf[ActorRef])
        .annotatedWith(Names.named("spray-can-root-service"))
        .toProvider(classOf[SprayCanRootServiceProvider])
        .in(Scopes.SINGLETON)
    bind(classOf[ActorRef])
        .annotatedWith(Names.named("http-server"))
        .toProvider(classOf[SprayCanProvider])
        .in(Scopes.SINGLETON)
  }
}
