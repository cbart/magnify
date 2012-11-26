package controllers

import magnify.features.Sources

/**
 * @author Cezary Bartoszuk (cezary@codilime.com)
 */
trait ProjectList {

  protected def sources: Sources

  implicit def projects: Projects =
    Projects(sources.list)
}
