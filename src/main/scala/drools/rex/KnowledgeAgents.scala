package drools.rex


import _root_.scala.{None, Nothing}
import java.io.InputStream
import java.lang.management.ManagementFactory
import java.util.Properties
import javax.management.ObjectName
import javax.servlet.ServletContext

import management.REXManagement
import org.drools.agent.{KnowledgeAgent, KnowledgeAgentFactory}
import scala.actors.Actor
import scala.actors.Actor._

import org.drools.io.ResourceFactory

/**
 * 
 * @author Michael Neale
 */

object KnowledgeAgents {


    val kaActor = actor {

     loop {
       receive {
         case (req: GetAgent) => {
             try {
               val existing = req.context.getAttribute(req.name)
               if (existing != null) {
                 reply { existing.asInstanceOf[KnowledgeAgent] }
               } else {
                 val ka = loadAgent(req.name, req.context.getInitParameter("agent-config-directory"))
                 req.context.setAttribute(req.name, ka)
                 reply { ka }
               }
             } catch { case e: Exception =>
                         req.context.log("Unable to load agent: " + req.name, e)
                         reply {None}   }

         }
       }
     }
    }


  /** return an agent, loading it into the context if necessary */
  def getAgent(name: String, ctx: ServletContext) : Option[KnowledgeAgent] = {
        kaActor !? GetAgent(name, ctx) match {
          case ka: KnowledgeAgent => Some(ka)
          case _ => None
        }
  }


  case class GetAgent(name: String, context: ServletContext)


  def loadAgent(name: String, configDir: String) : KnowledgeAgent = {

      val config = classOf[KnowledgeAgent].getResourceAsStream("/drools-agent.properties") match {
        case null => KnowledgeAgentFactory.newKnowledgeAgentConfiguration
        case ins: InputStream => {
          val props = new Properties
          props.load(ins)
          KnowledgeAgentFactory.newKnowledgeAgentConfiguration(props)
        }
      }

      val agentFile = if (name.startsWith("/")) name + ".xml" else "/" + name + ".xml"
      val ka = KnowledgeAgentFactory.newKnowledgeAgent(agentFile, config)

      if (configDir.startsWith("classpath:")) {
        ka.applyChangeSet(ResourceFactory.newClassPathResource(configDir.replace("classpath:", "") + agentFile))
      } else {
        ka.applyChangeSet(ResourceFactory.newUrlResource(configDir + agentFile))
      }

      REXManagement.registerNew(ka, configDir + agentFile)
      ka
  }


}