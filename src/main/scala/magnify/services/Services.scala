package magnify.services

import magnify.common.reflect.constructor
import magnify.features.{GraphRepository, Imports}
import magnify.model.graph.Graph

import akka.actor.ActorSystem
import com.google.inject.{Singleton, AbstractModule, Provides}

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
final class Services extends AbstractModule {
  def configure() {
    requireBinding(classOf[ActorSystem])
    bind(classOf[JavaParser]).toConstructor(constructor[ClassAndImportsParser])
    bind(classOf[Imports]).toConstructor(constructor[ExplicitProjectImports])
  }

  @Provides
  def readZipReader: (Array[Byte], String => Boolean) => Reader =
    new ZipReader(_, _)

  @Provides
  @Singleton
  def repository: GraphRepository =
    InMemoryRepository(Map.empty[String, Graph])
}
