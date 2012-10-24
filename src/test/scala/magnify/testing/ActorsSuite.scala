package magnify.testing

import akka.actor.ActorSystem
import org.scalatest.{BeforeAndAfterAll, Suite}

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
trait ActorsSuite extends BeforeAndAfterAll {
  this: Suite =>

  protected def system: ActorSystem

  override protected def afterAll() {
    try {
      super.afterAll()
    } finally {
      system.shutdown()
    }
  }
}
