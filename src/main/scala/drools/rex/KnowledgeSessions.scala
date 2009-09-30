package drools.rex

import javax.servlet.http.HttpSession
import javax.servlet.ServletContext
import org.drools.agent.KnowledgeAgent
import _root_.scala.{None, Nothing}

import org.drools.event.rule.WorkingMemoryEventManager
import org.drools.runtime.{StatelessKnowledgeSession, StatefulKnowledgeSession}
import org.drools.{KnowledgeBase}
import scala.actors.Actor
import scala.actors.Actor._

/**
 * 
 * @author Michael Neale
 */

object KnowledgeSessions {

  val KSESSION_KEY = "knowledge.session"

  val ksActor = actor {
   loop {
     receive {
       case (req: GetSession) => {
           try {
             val existing = req.session.getAttribute(KSESSION_KEY)
             if (existing != null) {
               reply { existing.asInstanceOf[StatefulKnowledgeSession] }
             } else {
               val kb = req.ka.getKnowledgeBase
               val ks = kb.newStatefulKnowledgeSession
               addLoopDetector(req.ka, ks, req.session.getServletContext)
               req.session.setAttribute(KSESSION_KEY, ks)
               reply { ks }
             }
           } catch { case e: Exception =>
                       req.session.getServletContext.log("Unable to load session", e)
                       reply {None}   }

       }
     }
   }
  }
  case class GetSession(ka: KnowledgeAgent, session: HttpSession)

  
  def getStatefulSession(ka: KnowledgeAgent, session: HttpSession) : Option[StatefulKnowledgeSession] = {
      ksActor !? GetSession(ka, session) match {
        case sess: StatefulKnowledgeSession => Some(sess)
        case _ => None
      }
  }




  def getStatelessSession(ka: KnowledgeAgent, ctx: ServletContext) : StatelessKnowledgeSession = {
    val kb = ka.getKnowledgeBase
    val session = kb.newStatelessKnowledgeSession
    addLoopDetector(ka, session, ctx)
    session
  }


  /**
   * Adds the loop detected if needed.
   */
  private def addLoopDetector(ka: KnowledgeAgent, session: WorkingMemoryEventManager, ctx: ServletContext) = {
    ctx.getInitParameter("loop-detection-enabled") match {
      case "true" => {
        session.addEventListener(
                    new LoopDetectorListener(1000, 250,
                      (msg: String) => ctx.log("!!!! NON TERMINATING LOOP DETECTED FOR " + ka.getName + " and terminated. " + msg))
                  )
      }
      case _ => session
    }
  }

  

}