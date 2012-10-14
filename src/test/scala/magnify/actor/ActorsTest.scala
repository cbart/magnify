package magnify.actor

import akka.actor.ActorSystem
import com.google.inject.Guice
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
@RunWith(classOf[JUnitRunner])
final class ActorsTest extends FunSuite with ShouldMatchers with BeforeAndAfterAll {
  val injector = Guice.createInjector(new Actors())

  def system = injector.getInstance(classOf[ActorSystem])

  test("should provide one and only one instance of ActorSystem") {
    system should be theSameInstanceAs(system)
  }

  override protected def afterAll() {
    system.shutdown()
    super.afterAll()
  }
}
