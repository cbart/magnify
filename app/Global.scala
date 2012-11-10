import play.api.GlobalSettings

import com.google.inject.Guice
import com.google.inject.Injector

object Global extends GlobalSettings {

  val injector: Injector = createInjector()

  override def getControllerInstance(controllerClass: Class[Nothing]): A = {
    injector.getInstance(controllerClass)
  }

  def createInjector(): Injector = {
    Guice.createInjector()
  }

}