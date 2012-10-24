package magnify.testing

import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
abstract class ActorsTestBase
    extends TestKit(ActorSystem())
    with FunSuite
    with ActorsSuite
    with ShouldMatchers
