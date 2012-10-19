package magnify.testing

import org.scalatest.{BeforeAndAfterAll, Suite}
import akka.testkit.TestKit

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
trait ActorsSuite extends BeforeAndAfterAll {
  this: TestKit with Suite =>

  override protected def afterAll() {
    try {
      super.afterAll()
    } finally {
      system.shutdown()
    }
  }
}
