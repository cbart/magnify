package magnify

import com.google.inject.{Key, Guice}

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
package object modules {
  private val injector = Guice.createInjector()

  def inject[A](implicit manifest: Manifest[A]): A =
    injector.getInstance(manifest.erasure.asInstanceOf[Class[A]])
}
