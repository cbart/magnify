package magnify.web.api.routes

import akka.actor._
import com.google.inject._
import com.google.inject.multibindings.Multibinder
import com.google.inject.name.{Named, Names}
import spray.routing._

import scala.collection.JavaConversions._

final class Routes extends PrivateModule {
  protected override def configure() {
    requireBinding(classOf[ActorSystem])
    expose(Key.get(classOf[ActorRef], Names.named("http-service")))
    val routeBinder = Multibinder.newSetBinder(binder(), new TypeLiteral[() => Route]() {})
    bindRoute[Control](routeBinder)
    bindRoute[Data](routeBinder)
    bindRoute[Frontend](routeBinder)
  }

  private def bindRoute[T <: (() => Route)](multiBinder: Multibinder[() => Route])
      (implicit manifest: Manifest[T]) {
    val cls = manifest.runtimeClass.asInstanceOf[Class[T]]
    val constructor = cls.getConstructor(classOf[ActorSystem])
    multiBinder.addBinding().toConstructor(constructor)
  }

  @Provides
  private def routes(factories: java.util.Set[() => Route]): Iterable[Route] =
    factories.map(_()).toSet

  @Provides
  @Named("http-service")
  private def httpService(system: ActorSystem, routes: Iterable[Route]): ActorRef =
    system.actorOf(Props(new RoutingActor(routes)), "http-service")
}