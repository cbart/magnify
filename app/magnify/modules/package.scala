package magnify

import com.google.inject.Guice
import magnify.features.Features
import magnify.services.Services

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
package object modules {
  private val injector = Guice.createInjector(new Features(), new Services())

  def inject[A](implicit manifest: Manifest[A]): A =
    injector.getInstance(manifest.erasure.asInstanceOf[Class[A]])
}
