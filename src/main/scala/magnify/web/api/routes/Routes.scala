package magnify.web.api.routes

import akka.actor.{ActorRef, ActorSystem}
import cc.spray.Route
import com.google.inject._
import com.google.inject.multibindings.Multibinder
import com.google.inject.name.{Named, Names}

import scala.collection.JavaConversions._

final class Routes extends PrivateModule {
  protected override def configure() {
    requireBinding(classOf[ActorSystem])
    expose(Key.get(classOf[ActorRef], Names.named("root-service")))
    val routeBinder = Multibinder.newSetBinder(binder(), new TypeLiteral[() => Route]() {})
    bindRoute[Control](routeBinder)
    bindRoute[Data](routeBinder)
    bindRoute[Frontend](routeBinder)
    bind(new TypeLiteral[() => ActorRef]() {}).annotatedWith(Names.named("root-service-provider"))
        .toConstructor(classOf[RootServiceProvider].getConstructor(classOf[ActorSystem], classOf[Iterable[Route]]))
  }

  private def bindRoute[T <: (() => Route)](multiBinder: Multibinder[() => Route])
      (implicit manifest: Manifest[T]) {
    val cls = manifest.erasure.asInstanceOf[Class[T]]
    val constructor = cls.getConstructor(classOf[ActorSystem])
    multiBinder.addBinding().toConstructor(constructor)
  }

  @Provides
  private def routes(factories: java.util.Set[() => Route]): Iterable[Route] =
    factories.map(_()).toSet

  @Provides
  @Named("root-service")
  private def rootService(@Named("root-service-provider") provider: () => ActorRef): ActorRef =
    provider()
}