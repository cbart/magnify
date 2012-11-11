package controllers

import magnify.features.Sources
import magnify.model.Zip
import magnify.modules.inject
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import scalaz.Scalaz._


object Project extends Project(inject[Sources])

sealed class Project (sources: Sources) extends Controller {
  def list = Action { implicit request =>
    Ok(views.html.list())
  }

  def newProject = Action { implicit request =>
    Ok(views.html.newProject())
  }

  private val allowedFormats = Set("application/zip", "application/x-java-archive")

  def upload = Action(parse.multipartFormData) { implicit request =>
    val name = request.body.dataParts.get("project-name").flatMap {
      case Seq(onlyName) => Some(onlyName)
      case _ => None
    }
    val src = request.body.file("project-sources").filter { file =>
      file.contentType.map(allowedFormats).getOrElse(false)
    }
    (name |@| src) { (name: String, src: FilePart[TemporaryFile]) =>
      val TemporaryFile(file) = src.ref
      sources.add(name, new Zip(file))
      name
    } match {
      case Some(name) =>
        Redirect(routes.Project.list())
          .flashing("success" -> "Project %s adding scheduled.".format(name))
      case None =>
        Redirect(routes.Project.newProject())
          .flashing("error" -> "Something went wrong. Project wasn't added.")
    }
  }
}