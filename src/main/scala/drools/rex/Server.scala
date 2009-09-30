package drools.rex

import java.io.InputStream
import java.util.Properties
import javax.servlet._
import javax.servlet.http._


import management.REXManagement
import org.drools.command.runtime.rule.ModifyCommand
import org.drools.io.{ResourceChangeScannerConfiguration, ResourceFactory}
import org.drools.command.runtime.rule.ModifyCommand
import org.drools.runtime.{StatefulKnowledgeSession, StatelessKnowledgeSession}

import org.drools.agent.KnowledgeAgent
import pages.{WSDL, StatusPage}
/**
 * 
 * @author Michael Neale
 */

class Server extends HttpServlet {
  var context : ServletContext = null
  override def init(sc: ServletConfig) = {
    context = sc.getServletContext
    ModifyCommand.ALLOW_MODIFY_EXPRESSIONS = false //to disallow arbitrary MVEL expressions
    REXManagement.init(context)
  }

  def execute(ag: KnowledgeAgent, ins: InputStream, ksession: Either[StatefulKnowledgeSession, StatelessKnowledgeSession], outs: ServletOutputStream, soap: Boolean) {
    val start = System.currentTimeMillis
    val responseXML = SessionExecutor.execute(ins, ksession, soap)
    REXManagement.recordExecutionTiming(ag, System.currentTimeMillis - start)
    outs.write(responseXML getBytes)
    outs.flush
  }

}

class StatelessServer extends Server {

  override def doGet(req: HttpServletRequest, res: HttpServletResponse) = {
    if (req.getQueryString.endsWith("wsdl")) {
      res.getOutputStream.write(WSDL.getWSDL.getBytes)
    } 
  }

  override def doPost(req: HttpServletRequest, res: HttpServletResponse) = {
    KnowledgeAgents getAgent(req.getPathInfo, context) match {
      case Some(agent: KnowledgeAgent) => {
        execute(agent, req.getInputStream, Right(KnowledgeSessions.getStatelessSession(agent, context)), res.getOutputStream, req.getHeader("SOAPAction") != null)
      }
      case None => res sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Unable to get the Knowledge Agent for request.")
    }
  }

}

class StatefulServer extends Server {
  override def doGet(req: HttpServletRequest, res: HttpServletResponse) = {
    if (req.getQueryString.endsWith("wsdl")) {
      res.getOutputStream.write(WSDL.getWSDL.getBytes)
    }
  }

  override def doPost(req: HttpServletRequest, res: HttpServletResponse) = {
    if (req.getRequestURI.contains("dispose-session")) {
      req.getSession.invalidate
      res.setStatus(HttpServletResponse.SC_ACCEPTED)
    } else {
      KnowledgeAgents getAgent(req.getPathInfo, context) match {
        case Some(agent) => {
          KnowledgeSessions.getStatefulSession(agent, req.getSession) match {
            case  Some(ksession: StatefulKnowledgeSession) => {
               execute(agent, req.getInputStream, Left(ksession), res.getOutputStream, req.getHeader("SOAPAction") != null)
            }
            case None => res sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Unable to get Knowledge Session for the request.")
          }
        }
        case None => res sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Unable to get the Knowledge Agent for request.")
      }
    }
  }


  override def doDelete(req: HttpServletRequest, res: HttpServletResponse) = {
    req.getSession invalidate
  }

}


class Status extends Server {
  override def doGet(req: HttpServletRequest, res: HttpServletResponse) = {
    res.getWriter.print ("OK")
  }
}


class StatefulListener extends HttpSessionListener {
  def sessionCreated(ce: HttpSessionEvent) = {}
  def sessionDestroyed(de: HttpSessionEvent) = {
    de.getSession.getAttribute(KnowledgeSessions.KSESSION_KEY) match {
      case ks: StatefulKnowledgeSession => ks.dispose
      case _ => None
    }
  }
}


class AgentListener extends ServletContextListener {
  def contextInitialized(ev: ServletContextEvent) = {
    ev.getServletContext.log("Starting knowledge agent service...")
    ResourceFactory.getResourceChangeNotifierService.start
    ResourceFactory.getResourceChangeScannerService.start
    classOf[KnowledgeAgent].getResourceAsStream("/scanner.properties") match {
        case null => ev.getServletContext.log("Using default scanner settings...")
        case ins: InputStream => {
          val props = new Properties
          props.load(ins)
          val conf = ResourceFactory.getResourceChangeScannerService.newResourceChangeScannerConfiguration(props);
          ResourceFactory.getResourceChangeScannerService.configure(conf)
          ev.getServletContext.log("Using scanner.properties")
        }
      }
  }
  def contextDestroyed(ev: ServletContextEvent) = {
    ev.getServletContext.log("Stopping knowledge agent service")
    ResourceFactory.getResourceChangeNotifierService.stop
    ResourceFactory.getResourceChangeScannerService.stop
  }
}



