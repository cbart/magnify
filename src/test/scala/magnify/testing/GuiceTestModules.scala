package magnify.testing

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.TestKit
import com.google.inject.{Key, AbstractModule}
import com.google.inject.name.Names
import org.mockito.Mockito

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

  protected def mockitoModule[A](implicit manifest: Manifest[A]): AbstractModule =
    new MockitoModule[A](manifest, Key.get(manifest.erasure.asInstanceOf[Class[A]]))

  protected final class MockitoModule[A] (manifest: Manifest[A], key: Key[A])
      extends AbstractModule {
    def configure() {
      bind(key).toInstance(Mockito.mock(manifest.erasure.asInstanceOf[Class[A]]))
    }
  }
}
