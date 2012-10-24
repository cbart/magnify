package magnify.web.api.routes

import akka.testkit.TestKitBase
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import spray.testkit.ScalatestRouteTest

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
trait RouteTestBase
    extends FunSuite
    with ScalatestRouteTest
    with ShouldMatchers
    with TestKitBase