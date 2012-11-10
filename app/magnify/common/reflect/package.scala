package magnify.common

import java.lang.reflect.Constructor

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
package object reflect {
  /**
   * Returns single constructor of `ActorRefModule`.
   *
   * @throws MatchError if `ActorRefModule` doesn't have only one constructor.
   */
  def constructor[A](implicit manifest: Manifest[A]): Constructor[A] = {
    val Array(onlyConstructor) = manifest.erasure.getConstructors
    onlyConstructor.asInstanceOf[Constructor[A]]
  }
}