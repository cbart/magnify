package magnify.testing

import akka.testkit.TestKit
import cc.spray.test.SprayTest

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
trait SpraySuite extends SprayTest {
  this: TestKit =>

  override val actorSystem = system
}
