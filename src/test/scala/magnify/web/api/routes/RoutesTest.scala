package magnify.web.api.routes

import akka.testkit.TestKit
import akka.actor.{ActorRef, ActorSystem}
import cc.spray.Route
import com.google.inject._
import com.google.inject.name.Names
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
@RunWith(classOf[JUnitRunner])
final class RoutesTest extends TestKit(ActorSystem()) with FunSuite with ShouldMatchers
    with BeforeAndAfterAll {

  val injector = Guice.createInjector(new ActorsModule(), new Routes)

  test("should inject root service actor") {
    val key = Key.get(classOf[ActorRef], Names.named("root-service"))
    injector.getInstance(key)
  }

  test("should not expose routes") {
    intercept[ConfigurationException] {
      val key = Key.get(new TypeLiteral[Set[Route]]() {})
      injector.getInstance(key)
    }
  }

  private final class ActorsModule extends AbstractModule {
    def configure() {
      bind(classOf[ActorSystem]).toInstance(system)
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
