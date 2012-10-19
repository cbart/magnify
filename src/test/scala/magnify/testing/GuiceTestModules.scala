package magnify.testing

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.TestKit
import com.google.inject.AbstractModule
import com.google.inject.name.Names

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
trait GuiceTestModules {
  this: TestKit =>

  protected final class ActorsModule extends AbstractModule {
    def configure() {
      bind(classOf[ActorSystem]).toInstance(system)
    }
  }

  protected final class MockActor (name: String) extends AbstractModule {
    def configure() {
      bind(classOf[ActorRef])
          .annotatedWith(Names.named(name))
          .toInstance(testActor)
    }
  }
}
