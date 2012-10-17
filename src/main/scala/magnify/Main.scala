package magnify

import magnify.actor.Actors
import magnify.web.api.routes.Routes
import magnify.web.api.view.Views
import magnify.web.http.Http

import akka.actor.ActorRef
import cc.spray.can.server.HttpServer
import com.google.inject.{Guice, Key}
import com.google.inject.name.Names
import magnify.core.Core

object Main extends App {
  val injector = Guice.createInjector(
      new Actors(),
      new Core(),
      new Routes(),
      new Views(),
      new Http()
  )

  val httpServer = injector.getInstance(Key.get(classOf[ActorRef], Names.named("http-server")))

  httpServer ! HttpServer.Bind("localhost", 8080)
}