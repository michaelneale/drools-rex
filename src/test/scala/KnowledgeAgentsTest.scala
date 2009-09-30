import drools.rex.management.REXManagement
import drools.rex.{KnowledgeAgents}
import java.io.File
import java.util.HashMap
import javax.management.ObjectName
import javax.servlet.ServletContext
import javax.servlet.ServletContext
import javax.servlet.ServletContext
import javax.servlet.ServletContext
import junit.framework.TestCase
import junit.framework.Assert._
import management.ManagementFactory
import org.drools.agent.KnowledgeAgent

/**
 * 
 * @author Michael Neale
 */

class KnowledgeAgentsTest extends Utils {

  def testLoading() = {


 
    val rl = new File("rule.drl")

    val drl = "package foo.bar\n declare Cheese \n name: String \n price: Integer \n end \n rule 'whee' \n when \n c: Cheese() \n then \n c.setPrice(42); \n System.out.println(42);\n end";
    writeFile(rl, drl)

    val xml = <change-set xmlns='http://drools.org/drools-5.0/change-set' xmlns:xs='http://www.w3.org/2001/XMLSchema-instance' xs:schemaLocation='http://drools.org/drools-5.0/change-set drools-change-set-5.0.xsd' >
                <add>
                  <resource source={rl.toURI.toURL.toString} type='DRL' />
                </add>
              </change-set>

    val cs = new File("changeSet.xml")
    writeFile(cs, xml.toString)

    val agentPath = cs.toURI.toURL.toString.replace("/changeSet.xml", "")
    val name = "changeSet"

    val attributes = new HashMap[String, Object]
    val ctx = new MockServletContext(Map("agent-config-directory" -> agentPath, "loop-detection-enabled" -> "false"), attributes)
    val ka = KnowledgeAgents getAgent(name, ctx)
    assertNotNull(ka)
    assertEquals(1, attributes size)
    assertFalse(ctx calledError)
    ka match {
      case Some(k: KnowledgeAgent) => {
        val kb = k.getKnowledgeBase
        val rules = kb.getKnowledgePackage("foo.bar").getRules
        assertEquals(1, rules.size)
      }
      case None => fail("Should have had a knowledge agent")
    }

    attributes put("whee", "bad stuff")
    val ka_ = KnowledgeAgents getAgent("whee", ctx)
    assertTrue(ctx calledError)
    ka_ match { case None => println("OK") case _ => fail("should have been none")}


 


  }

  def testFromClasspath = {
    val agentPath = "classpath:changesets"
    val attributes = new HashMap[String, Object]
    val ctx = new MockServletContext(Map("agent-config-directory" -> agentPath, "loop-detection-enabled" -> "false"), attributes)
    val ka = KnowledgeAgents getAgent("demo_changeset", ctx)
    assertNotNull(ka)
    
    assertFalse(ctx calledError)
    ka match {
      case Some(k: KnowledgeAgent) => {
        val kb = k.getKnowledgeBase
        val rules = kb.getKnowledgePackage("foo.bar").getRules
        assertEquals(1, rules.size)
      }
      case None => fail("Should have had a knowledge agent")
    }

  }

  def testJMX = {
    val initParams = Map("foo" -> "bar")
    val attributes = new java.util.HashMap[String, Object]
    val ctx = new MockServletContext(initParams, attributes)
    REXManagement.init(ctx)
    val ka = getDefaultKA
    REXManagement.registerNew(ka, "weewaah")


    val nm = ManagementFactory.getPlatformMBeanServer.getAttribute(new ObjectName("org.drools.rex:type=KnowledgeBase,name=" + ka.getName), "NumberOfRules")
    assertEquals(1, nm)

    val time = ManagementFactory.getPlatformMBeanServer.getAttribute(new ObjectName("org.drools.rex:type=KnowledgeBase,name=" + ka.getName), "LastExecutionTime")
    assertNotNull(time)

    REXManagement.recordExecutionTiming(ka, 42)
    val time_ = ManagementFactory.getPlatformMBeanServer.getAttribute(new ObjectName("org.drools.rex:type=KnowledgeBase,name=" + ka.getName), "LastExecutionTime")
    assertEquals(42L, time_)

    val cs = ManagementFactory.getPlatformMBeanServer.getAttribute(new ObjectName("org.drools.rex:type=KnowledgeBase,name=" + ka.getName), "ChangeSetURL")
    assertEquals("weewaah", cs)

    val pkgs = ManagementFactory.getPlatformMBeanServer.getAttribute(new ObjectName("org.drools.rex:type=KnowledgeBase,name=" + ka.getName), "Packages").asInstanceOf[Array[String]]


    assertEquals(1, pkgs.size)
    assertTrue(pkgs(0).indexOf("foo.bar") > -1)

    ManagementFactory.getPlatformMBeanServer.invoke(new ObjectName("org.drools.rex:type=KnowledgeBase,name=" + ka.getName), "reset", null, null)
    assertEquals(0, attributes.size)





  }



}