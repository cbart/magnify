package magnify.services.project.repository

import akka.actor._
import com.google.inject.{Inject, Provider}
import com.tinkerpop.blueprints.Graph

import scala.collection.mutable

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
private[services] class Repository @Inject() (actorSystem: ActorSystem) extends Provider[ActorRef] {
  override def get: ActorRef = actorSystem.actorOf(
    props = Props(
      new Actor {
        private val projects = mutable.Map.empty[String, Graph]

        override protected def receive = {
          case AddProject(name, graph) => projects.put(name, graph)
          case GetGraph(name, continuation) => continuation(projects.get(name))
          case ListProjects(continuation) => continuation(projects.keys.toSeq)
        }
      }
    ),
    name = "project-repository"
  )
}
