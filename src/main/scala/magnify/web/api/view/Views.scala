package magnify.web.api.view

import magnify.web.api.view.json.JsonView

import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.{AbstractModule, Scopes}
import com.google.inject.name.Names

final class Views extends AbstractModule {
  protected override def configure() {
    requireBinding(classOf[ActorSystem])
    bind(classOf[ActorRef])
        .annotatedWith(Names.named("json-view"))
        .toProvider(classOf[JsonView])
        .in(Scopes.SINGLETON)
  }
}
