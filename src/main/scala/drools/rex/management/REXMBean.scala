package drools.rex.management


import java.lang.management.ManagementFactory
import java.util.Collections
import javax.management.openmbean.TabularData
import javax.management.ObjectName
import javax.servlet.ServletContext
import org.drools.agent.KnowledgeAgent
import org.drools.definition.KnowledgePackage
import scala.actors.Actor
import scala.actors.Actor._

/**
 * A simple MBean/JMX based management tool.
 * @author Michael Neale
 */

trait REXMonitorMBean {
  def getPackages: Array[String]
  def getNumberOfRules: java.lang.Integer
  def getNumberOfProcesses: java.lang.Integer
  def getLastExecutionTime: java.lang.Long
  def reset: Unit //force a reload
  def getChangeSetURL: String
}



object REXManagement {
  var context : ServletContext = null

  def init(ctx: ServletContext) = {context = ctx}



  def registerNew(ka: KnowledgeAgent, configUrl: String) = {
    val mbean = new REXMonitor(context, ka, configUrl)
    ManagementFactory.getPlatformMBeanServer.registerMBean(mbean, new ObjectName("org.drools.rex:type=KnowledgeBase,name=" + ka.getName))
  }


  val timer = actor {
                val timings = new java.util.HashMap[String, Long]
                loop {
                  receive {
                    case (time: Long, ka: KnowledgeAgent) => {
                      timings.put(ka.getName, time)
                    }
                    case (ka: KnowledgeAgent) => reply {timings.get(ka.getName)}
                  }
                }
              }
  


  def recordExecutionTiming(ag: KnowledgeAgent, time: Long) = timer ! (time, ag)

  def getTiming(ka: KnowledgeAgent) : Long = {
    timer !? ka match {
      case (time: Long) => time
      case _ => 0
    }
  }
  
}

class REXMonitor(ctx: ServletContext, ka: KnowledgeAgent, configUrl: String) extends REXMonitorMBean {
  def reset = {
    ctx.removeAttribute(ka.getName)
    ManagementFactory.getPlatformMBeanServer.unregisterMBean(new ObjectName("org.drools.rex:type=KnowledgeBase,name=" + ka.getName))
  }

  def getPackages = toArray(ka.getKnowledgeBase.getKnowledgePackages).map(_.getName + " ")
  def getNumberOfRules =  toArray(ka.getKnowledgeBase.getKnowledgePackages).foldLeft(0)(_ + _.getRules.size)
  def getNumberOfProcesses =  toArray(ka.getKnowledgeBase.getKnowledgePackages).foldLeft(0)(_ + _.getProcesses.size)
  def getChangeSetURL = configUrl
  def getLastExecutionTime =     REXManagement.getTiming(ka)
  
  private def toArray(col: java.util.Collection[KnowledgePackage]) = col.toArray(new Array[KnowledgePackage](col.size))
}



