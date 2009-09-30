package drools.rex


import javax.servlet.http.HttpSession
import org.drools.agent.{KnowledgeAgent, KnowledgeAgentFactory}
import org.drools.io.ResourceFactory
import org.drools.runtime.StatefulKnowledgeSession

/**
 * This is for when using the execution server as a library in other systems
 *
 * @author Michael Neale
 */
class ExecutionServerHelper(val session: HttpSession) {
    val KNOWLEDGE_SESSION = "knowledge.session"
    val AGENT_CONFIG_DIRECTORY = "agent-config-directory"
  
  	def getKnowledgeSession =	session.getAttribute(KNOWLEDGE_SESSION).asInstanceOf[StatefulKnowledgeSession]
    
	  def removeKnowledgeSession = {
      getKnowledgeSession match {
        case knowledgeSession: StatefulKnowledgeSession => {
           knowledgeSession.dispose
           session.removeAttribute(KNOWLEDGE_SESSION)
        }
      }
	  }

    def newKnowledgeSession(agentName: String) : StatefulKnowledgeSession = {
      removeKnowledgeSession
      val agentFile = "/" + agentName + ".xml"
      val agentConfigDir = session.getServletContext().getInitParameter(AGENT_CONFIG_DIRECTORY)
      val knowledgeAgent = KnowledgeAgentFactory.newKnowledgeAgent(agentFile)
      if (agentConfigDir.startsWith("classpath:")) {
        knowledgeAgent.applyChangeSet(ResourceFactory.newClassPathResource(agentConfigDir.replace("classpath:", "") + agentFile))
      } else {
        knowledgeAgent.applyChangeSet(ResourceFactory.newUrlResource(agentConfigDir + agentFile))
      }
      val knowledgeSession = knowledgeAgent.getKnowledgeBase.newStatefulKnowledgeSession
      session.setAttribute(KNOWLEDGE_SESSION, knowledgeSession);
      return knowledgeSession
    }

}