package magnify.web.api.routes

import magnify.common.guice.constructor
import magnify.web.api.controllers.DataController

import akka.actor._
import com.google.inject._
import com.google.inject.multibindings.Multibinder
import com.google.inject.name.{Named, Names}
import spray.routing._

import scala.collection.JavaConversions._

final class Routes extends PrivateModule {
  protected override def configure() {
    expose(Key.get(classOf[ActorRef], Names.named("http-service")))
    requireBinding(classOf[ActorSystem])
    requireBinding(classOf[DataController])
    val routeBinder = Multibinder.newSetBinder(binder(), new TypeLiteral[() => Route]() {})
    routeBinder.addBinding().toConstructor(constructor[Control])
    routeBinder.addBinding().toConstructor(constructor[Data])
    routeBinder.addBinding().toConstructor(constructor[Frontend])
  }

  @Provides
  private def routes(factories: java.util.Set[() => Route]): Iterable[Route] =
    factories.map(_()).toSet

  @Provides
  @Named("http-service")
  private def httpService(system: ActorSystem, routes: Iterable[Route]): ActorRef =
    system.actorOf(Props(new RoutingActor(routes)), "http-service")
}