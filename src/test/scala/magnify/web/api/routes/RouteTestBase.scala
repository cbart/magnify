package magnify.web.api.routes

import akka.testkit.TestKit
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import spray.testkit.ScalatestRouteTest
import akka.actor.ActorSystem

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
trait RouteTestBase
    extends FunSuite
    with ScalatestRouteTest
    with ShouldMatchers