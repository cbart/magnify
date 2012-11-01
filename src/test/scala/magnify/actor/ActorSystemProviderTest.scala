package magnify.actor

import akka.util.duration._
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
@RunWith(classOf[JUnitRunner])
final class ActorSystemProviderTest extends FunSuite with ShouldMatchers {
  test("actor system provided should shut down when hooks are executed") {
    val provider = new ActorSystemProvider({body => body})
    val system = provider()
    system.awaitTermination(2.seconds)
    system should be('terminated)
  }

  test("but if hook does not run then the actor system should not be terminated") {
    val provider = new ActorSystemProvider({body => })
    val system = provider()
    system should not be('terminated)
    system.shutdown()
  }
}
