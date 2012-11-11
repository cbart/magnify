package magnify.services

import com.google.inject.AbstractModule
import magnify.common.reflect.constructor
import magnify.features.{Parser, Imports}


/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
final class Services extends AbstractModule {
  def configure() {
    bind(classOf[Parser]).toConstructor(constructor[ClassAndImportsParser])
    bind(classOf[Imports]).toConstructor(constructor[ExplicitProjectImports])
  }
}
