package magnify.web.api.routes


import org.junit.runner.RunWith
import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar._
import spray.http._
import spray.http.MediaTypes._
import spray.routing.RequestContext

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
@RunWith(classOf[JUnitRunner])
final class DataTest extends RouteTestBase {
  val controller = mock[DataController]

  val route = new Data(system, controller).apply

  when(controller.uploadZipSrc("a", "xxx".getBytes("utf-8")))
    .thenAnswer(new Answer[RequestContext => Unit] {
    def answer(invocation: InvocationOnMock) =
      _.complete("Answer from mock!")
  })

  val formData = MultipartFormData(Map(
    "projectName" -> BodyPart(HttpBody("a")),
    "Filedata" -> BodyPart(HttpBody(`application/zip`, "xxx"))
  ))

  test("should invoke controller#uploadZipSrc") {
    Post("/data/projects/", Some(formData)) ~> route ~> check {
      entityAs[String] should equal("Answer from mock!")
    }
  }
}
