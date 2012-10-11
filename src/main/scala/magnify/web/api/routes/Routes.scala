package magnify.web.api.routes

import akka.actor.{ActorRef, ActorSystem}
import cc.spray.Route
import com.google.inject._
import com.google.inject.multibindings.Multibinder
import com.google.inject.name.Names

final class Routes extends AbstractModule {
  protected override def configure() {
    requireBinding(classOf[ActorSystem])
    val routeBinder =
      Multibinder.newSetBinder(binder(), new TypeLiteral[Route]() {}, Names.named("routes"))
    routeBinder.addBinding().toProvider(classOf[Control]).in(Scopes.SINGLETON)
    routeBinder.addBinding().toProvider(classOf[Data]).in(Scopes.SINGLETON)
    routeBinder.addBinding().toProvider(classOf[Frontend]).in(Scopes.SINGLETON)
    bind(classOf[ActorRef])
        .annotatedWith(Names.named("root-service"))
        .toProvider(classOf[RootServiceProvider])
        .in(Scopes.SINGLETON)
  }
}

private class OneProvider extends Provider[String] {
  override def get = "One"
}

private class TwoProvider extends Provider[String] {
  override def get = "Two"
}