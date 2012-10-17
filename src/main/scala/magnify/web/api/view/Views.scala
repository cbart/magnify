package magnify.web.api.view

import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.{AbstractModule, Scopes}
import com.google.inject.name.Names

final class Views extends AbstractModule {
  protected override def configure() {
    requireBinding(classOf[ActorSystem])
    bind(classOf[ActorRef])
        .annotatedWith(Names.named("view"))
        .toProvider(classOf[View])
        .in(Scopes.SINGLETON)
  }
}
