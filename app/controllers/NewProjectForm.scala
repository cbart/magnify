package controllers

import play.api.data.Forms._

/**
 * @author Cezary Bartoszuk (cezary@codilime.com)
 */
final class NewProjectForm extends AbstractForm[String] (
  single("project-name" -> nonEmptyText(minLength = 5))
)
