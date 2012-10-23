package magnify

import magnify.actor.Actors
import magnify.web.api.routes.Routes
import magnify.web.api.view.Views
import magnify.web.http.Http

import akka.actor.ActorRef
import com.google.inject.{Guice, Key}
import com.google.inject.name.Names
import magnify.features.Features
import magnify.services.Services
import spray.can.server.HttpServer

object Main extends App {
  val injector = Guice.createInjector(
      new Actors(),
      new Features(),
      new Services(),
      new Routes(),
      new Views(),
      new Http()
  )

  val httpServer = injector.getInstance(Key.get(classOf[ActorRef], Names.named("http-server")))

  httpServer ! HttpServer.Bind("localhost", 8080)
}