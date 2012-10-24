package magnify.web.api.routes

import magnify.testing.{ActorsTestBase, GuiceTestModules, ActorsSuite}

import akka.actor.ActorRef
import com.google.inject._
import com.google.inject.name.Names
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import spray.routing.Route

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
@RunWith(classOf[JUnitRunner])
final class RoutesTest extends ActorsTestBase with GuiceTestModules {
  val injector = Guice.createInjector(new ActorsModule(), new Routes)

  test("should inject http service actor") {
    injector.getInstance(Key.get(classOf[ActorRef], Names.named("http-service")))
  }

  test("should not expose routes") {
    intercept[ConfigurationException] {
      val key = Key.get(new TypeLiteral[Set[Route]]() {})
      injector.getInstance(key)
    }
  }
}
