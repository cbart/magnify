package magnify.testing

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.TestKit
import com.google.inject.{Module, Key, AbstractModule}
import com.google.inject.name.Names
import org.mockito.Mockito

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
trait GuiceTestModules {
  this: TestKit =>

  /**
   * Returns module providing implicitly found `ActorSystem`.
   */
  protected def systemModule(implicit system: ActorSystem): Module =
    new ActorsModule(system)

  protected final class ActorsModule (system: ActorSystem) extends AbstractModule {
    def configure() {
      bind(classOf[ActorSystem]).toInstance(system)
    }
  }

  /**
   * Returns module injecting `name`d actor pointing to `testActor` from `TestKit`.
   */
  protected def mockActorModule(name: String): Module =
    new ActorRefModule(testActor, name)

  private final class ActorRefModule (actorRef: ActorRef, name: String) extends AbstractModule {
    def configure() {
      bind(classOf[ActorRef])
          .annotatedWith(Names.named(name))
          .toInstance(actorRef)
    }
  }

  /**
   * Returns module injecting mock of `ActorRefModule`.
   */
  protected def mockitoModule[A](implicit manifest: Manifest[A]): Module = {
    val cls = manifest.erasure.asInstanceOf[Class[A]]
    new MockitoModule[A](cls, Key.get(cls))
  }

  private final class MockitoModule[A] (cls: Class[A], key: Key[A])
      extends AbstractModule {
    def configure() {
      bind(key).toInstance(Mockito.mock(cls))
    }
  }
}
