package magnify.web.http

import magnify.testing.{ActorsTestBase, GuiceTestModules}

import akka.actor.ActorRef
import com.google.inject._
import com.google.inject.name.Names
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
@RunWith(classOf[JUnitRunner])
final class HttpTest extends ActorsTestBase with GuiceTestModules {
  test("should fail to create injector without root service") {
    intercept[CreationException] {
      Guice.createInjector(new Http(), systemModule)
    }
  }

  test("should succeed to create injector with actor system and root service provided") {
    Guice.createInjector(new Http(), systemModule, mockActorModule("http-service"))
  }

  test("should expose http server actor") {
    val injector = Guice.createInjector(new Http(), systemModule, mockActorModule("http-service"))
    injector.getInstance(Key.get(classOf[ActorRef], Names.named("http-server")))
  }
}
