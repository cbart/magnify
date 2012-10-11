package magnify.web.api.routes

import org.scalatest.{BeforeAndAfterAll, FunSuite}
import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.matchers.ShouldMatchers
import com.google.inject._
import cc.spray.Route
import com.google.inject.name.Names
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import scala.collection.JavaConversions._

/**
 *
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
@RunWith(classOf[JUnitRunner])
class RoutesTest extends TestKit(ActorSystem()) with FunSuite with ShouldMatchers
    with BeforeAndAfterAll {

  val injector = Guice.createInjector(new ActorsModule(), new Routes)

  test("should inject Set[Route] containing three routes") {
    val key = Key.get(new TypeLiteral[java.util.Set[Route]]() {}, Names.named("routes"))
    injector.getInstance(key).toSet should have ('size (3))
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
