package controllers

import play.api._
import play.api.mvc._
import magnify.features.SourceUseCase
import magnify.services.Reader
import javax.inject.Inject
import play.api.libs.Files.TemporaryFile
import java.io.{InputStream, FileInputStream}
import scala.io.Source

final class Application (sources: SourceUseCase, unzip: (InputStream, String => Boolean) => Reader)
    extends Controller {
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def uploadZip = Action(parse.multipartFormData) { request =>
    val zip = request.body.file("Filedata")
    val name = request.body.asFormUrlEncoded.get("projectName").map(_.head)
    if (zip.isDefined && name.isDefined) {
      val TemporaryFile(zipFile) = zip.get.ref
      val reader = unzip(new FileInputStream(zipFile), onlyJavaFiles)
      sources.add(name.get, reader)
      Ok("OK")
    } else {
      Redirect(routes.Application.index).flashing(
        "error" -> "Missing file"
      )
    }
  }

  private val onlyJavaFiles: String => Boolean = {
    filename => filename.endsWith(".java") && !filename.endsWith("Test.java")
  }
}