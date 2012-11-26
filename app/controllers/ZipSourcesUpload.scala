package controllers

import java.io.File
import java.lang.String
import magnify.features.Sources
import magnify.model.Zip
import magnify.modules.inject
import play.api.libs.Files
import play.api.libs.Files.TemporaryFile
import play.api.mvc._
import scala.Some

/**
 * @author Cezary Bartoszuk (cezary@codilime.com)
 */
object ZipSourcesUpload extends ZipSourcesUpload(inject[Sources])

sealed class ZipSourcesUpload (protected override val sources: Sources)
    extends Controller with ProjectList {

  private type MultipartRequest = Request[MultipartFormData[Files.TemporaryFile]]

  private val allowedFormats = Set("application/zip", "application/x-java-archive")

  private val success = "success" -> "Project was added."

  private val failure = "error" -> "Something went wrong. Please try again."

  def form = Action { implicit request =>
    Ok(views.html.newProject())
  }

  def upload = Action(parse.multipartFormData) { implicit request =>
    (for (name <- projectName; file <- projectSources) yield {
      sources.add(name, new Zip(file))
      uploaded(success)
    }) getOrElse uploaded(failure)
  }

  private def projectName(implicit request: MultipartRequest): Option[String] =
    request.body.dataParts.get("project-name").flatMap {
      case Seq(onlyName) => Some(onlyName)
      case _ => None
    }

  private def projectSources(implicit request: MultipartRequest): Option[File] =
    for {
      filePart <- request.body.file("project-sources")
      if filePart.contentType.map(allowedFormats) getOrElse false
      MultipartFormData.FilePart(_, _, _, TemporaryFile(file)) = filePart
    } yield file

  private def uploaded(status: (String, String)): Result =
    Redirect(routes.ZipSourcesUpload.form()).flashing(status)
}
