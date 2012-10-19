package magnify.web.http

import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import akka.testkit.TestKit
import akka.actor.{ActorRef, ActorSystem}
import org.scalatest.matchers.ShouldMatchers
import com.google.inject._
import com.google.inject.name.Names

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
@RunWith(classOf[JUnitRunner])
final class HttpTest extends TestKit(ActorSystem()) with FunSuite with ShouldMatchers
    with BeforeAndAfterAll {
  test("should fail to create injector without root service") {
    intercept[CreationException] {
      Guice.createInjector(new Http(), new ActorsModule())
    }
  }

  test("should succeed to create injector with actor system and root service provided") {
    Guice.createInjector(new Http(), new ActorsModule(), new MockActor("root-service"))
  }

  test("should expose http server actor") {
    val injector = Guice.createInjector(new Http(), new ActorsModule(),
      new MockActor("root-service"))
    injector.getInstance(Key.get(classOf[ActorRef], Names.named("http-server")))
  }

  private final class ActorsModule extends AbstractModule {
    def configure() {
      bind(classOf[ActorSystem]).toInstance(system)
    }
  }

  private final class MockActor (name: String) extends AbstractModule {
    def configure() {
      bind(classOf[ActorRef])
          .annotatedWith(Names.named(name))
          .toInstance(testActor)
    }
  }

  override protected def afterAll() {
    try {
      system.shutdown()
    } finally {
      super.afterAll()
    }
  }
}
