package magnify.features

import magnify.common.reflect.constructor

import com.google.inject.AbstractModule

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
final class Features extends AbstractModule {
  def configure() {
    requireBinding(classOf[Imports])
    requireBinding(classOf[Parser])
    bind(classOf[Sources]).toConstructor(constructor[GraphSources])
  }
}
