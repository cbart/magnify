package magnify.web.api.controllers

import magnify.common.guice.constructor
import magnify.features.SourceUseCase
import magnify.services.{JavaParser, Reader}

import akka.actor.ActorSystem
import com.google.inject.{AbstractModule, Key, TypeLiteral}

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
final class Controllers extends AbstractModule {
  override def configure() {
    requireBinding(classOf[ActorSystem])
    requireBinding(classOf[SourceUseCase])
    requireBinding(classOf[JavaParser])
    requireBinding(Key.get(new TypeLiteral[(Array[Byte], String => Boolean) => Reader]() {}))
    bind(classOf[DataController]).toConstructor(constructor[DataController])
  }
}
