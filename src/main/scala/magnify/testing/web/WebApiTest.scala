package magnify.testing.web

import magnify.web.api

import akka.util.Duration
import akka.util.duration._
import cc.spray.RequestContext
import cc.spray.http.{HttpMethod, HttpResponse, HttpRequest, HttpMethods}
import cc.spray.test.SprayTest
import cc.spray.typeconversion.DefaultUnmarshallers

trait WebApiTest extends SprayTest with DefaultUnmarshallers {
  this: api.Module =>

  protected val get = request(HttpMethods.GET) _

  protected val put = request(HttpMethods.PUT) _

  protected val post = request(HttpMethods.POST) _

  protected def request(method: HttpMethod)(uri: String) =
    testRoot(HttpRequest(uri = uri, method = method))

  private def testRoot(request: HttpRequest): HttpResponse = {
    val routeResult = new RouteResult
    rootService ! RequestContext(request = request, responder = routeResult.requestResponder,
      unmatchedPath = request.path)
    routeResult.awaitResult(timeout)
    new ServiceResultWrapper(routeResult, timeout).response
  }

  implicit protected def getStringContent(httpResponse: HttpResponse) = new {
    def asString = {
      val Some(content) = httpResponse.content
      val Right(asString) = content.as[String]
      asString
    }
  }

  protected def timeout: Duration = 10.seconds
}
