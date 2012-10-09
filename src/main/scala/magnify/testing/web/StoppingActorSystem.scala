package magnify.testing.web

import org.scalatest.{Suite, BeforeAndAfterAll}
import akka.testkit.TestKit

/**
 * Mixing this trait results in stopping the `ActorSystem` after running all the tests.
 *
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
trait StoppingActorSystem extends BeforeAndAfterAll {
  this: Suite with TestKit =>

  override protected def afterAll() {
    super.afterAll()
    system.shutdown()
  }
}
