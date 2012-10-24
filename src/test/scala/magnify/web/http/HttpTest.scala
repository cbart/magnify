package magnify.web.http

import magnify.testing.{ActorsSuite, GuiceTestModules}

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.TestKit
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import com.google.inject._
import com.google.inject.name.Names

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
@RunWith(classOf[JUnitRunner])
final class HttpTest extends TestKit(ActorSystem()) with FunSuite with ActorsSuite
    with ShouldMatchers with GuiceTestModules {
  test("should fail to create injector without root service") {
    intercept[CreationException] {
      Guice.createInjector(new Http(), new ActorsModule())
    }
  }

  test("should succeed to create injector with actor system and root service provided") {
    Guice.createInjector(new Http(), new ActorsModule(), new MockActor("http-service"))
  }

  test("should expose http server actor") {
    val injector = Guice.createInjector(new Http(), new ActorsModule(),
      new MockActor("http-service"))
    injector.getInstance(Key.get(classOf[ActorRef], Names.named("http-server")))
  }
}
