package magnify.web.http

import akka.actor._
import com.google.inject._
import com.google.inject.name.{Named, Names}
import spray.can.server.HttpServer
import spray.io.{IOBridge, SingletonHandler}

/**
 * Web server actors.
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
final class Http extends PrivateModule {
  protected override def configure() {
    requireBinding(classOf[ActorSystem])
    requireBinding(Key.get(classOf[ActorRef], Names.named("http-service")))
  }

  @Provides
  private def ioBridge(system: ActorSystem): IOBridge = {
    val ioBridge = new IOBridge(system).start()
    system.registerOnTermination {
      ioBridge.stop()
    }
    ioBridge
  }

  @Exposed
  @Provides
  @Named("http-server")
  private def httpServer(system: ActorSystem, ioBridge: IOBridge,
      @Named("http-service") httpService: ActorRef): ActorRef =
    system.actorOf(Props(new HttpServer(ioBridge, SingletonHandler(httpService))), "http-server")
}
