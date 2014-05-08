package controllers

import java.io.File

import scala.Some
import scala.Predef._

import magnify.features.Sources
import magnify.modules._
import play.api.libs.Files
import play.api.libs.Files.TemporaryFile
import play.api.mvc._

/**
 * @author Cezary Bartoszuk (cezary@codilime.com)
 */
object UploadCallCount extends UploadCallCount(inject[Sources])

sealed class UploadCallCount (override protected val sources: Sources)
    extends Controller with ProjectList {

  private type MultipartRequest = Request[MultipartFormData[Files.TemporaryFile]]

  private val success = "success" -> "Runtime data uploaded."

  private val failure = "error" -> "Something went wrong. Please try again."

  def form(name: String) = Action { implicit request =>
    sources.get(name) match {
      case Some(_) => Ok(views.html.uploadCallCount(name))
      case None => projectNotFound(name)
    }
  }

  def upload(name: String) = Action(parse.multipartFormData) { implicit request =>
    sources.get(name) match {
      case Some(_) =>
        (for (file <- projectSources) yield {
          sources.addRuntime(name, file)
          Redirect(routes.ShowGraph.show(name)).flashing(success)
        }) getOrElse Redirect(routes.UploadCallCount.form(name)).flashing(failure)
      case None =>
        projectNotFound(name)
    }
  }

  private def projectSources(implicit request: MultipartRequest): Option[File] =
    for {
      filePart <- request.body.file("project-sources")
      MultipartFormData.FilePart(_, _, _, TemporaryFile(file)) = filePart
    } yield file

  private def projectNotFound(projectName: String)(implicit request: Request[_]): Result = {
    val message = "Project \"%s\" was not found. Why not create new one?" format projectName
    Redirect(routes.ZipSourcesUpload.form()).flashing("warning" -> message)
  }
}
