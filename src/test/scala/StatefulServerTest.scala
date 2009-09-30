import drools.rex.{StatefulListener, StatefulServer, StatelessServer}
import java.io.{ByteArrayOutputStream, ByteArrayInputStream, File}
import java.lang.String
import java.util.{Map => JavaMap, HashMap}
import org.drools.event.process.ProcessEventListener
import org.drools.event.rule.{AgendaEventListener, WorkingMemoryEventListener}

import org.drools.runtime.rule.{AgendaFilter, FactHandle}
import org.drools.runtime.{ExitPoint, ObjectFilter, StatefulKnowledgeSession}
import org.drools.command.Command
import javax.servlet.http.HttpSessionEvent
import javax.servlet.ServletConfig
import junit.framework.TestCase
import junit.framework.Assert._


import org.drools.time.SessionClock
class StatefulServerTest extends Utils {

  def testStatefulEx = {
    val ss = new StatefulServer

    val rl = new File("rule.drl")

    val drl = "package foo.bar\n declare Cheese \n name: String \n price: Integer \n end \n rule 'whee' \n when \n c: Cheese() \n then \n c.setPrice(42); \n System.out.println(42);\n end";
    writeFile(rl, drl)

    val xml = <change-set xmlns='http://drools.org/drools-5.0/change-set' xmlns:xs='http://www.w3.org/2001/XMLSchema-instance' xs:schemaLocation='http://drools.org/drools-5.0/change-set drools-change-set-5.0.xsd' >
                <add>
                  <resource source={rl.toURI.toURL.toString} type='DRL' />
                </add>
              </change-set>

    val cs = new File("mystatefulrules.xml")
    writeFile(cs, xml.toString)

    val agentPath = cs.toURI.toURL.toString.replace("/mystatefulrules.xml", "")


    val attributes = new HashMap[String, Object]
    val ctx = new MockServletContext(Map("agent-config-directory" -> agentPath, "loop-detection-enabled" -> "false"), attributes)

    val config = new MockServletConfig(ctx)

    val name = "mystatefulrules"


    val xmlPost = <batch-execution>
                  <insert out-identifier='something'>
                    <foo.bar.Cheese>
                      <name>stilton</name>
                      <price>25</price>
                    </foo.bar.Cheese>
                  </insert>
              </batch-execution>


    val httpSession = new MockHttpSession(ctx)
    val req = new MockHttpRequest("/mystatefulrules", new ByteArrayInputStream(xmlPost.toString.getBytes), httpSession)
    val bout = new ByteArrayOutputStream
    val out = new MockServletOutputStream(bout)
    val res = new MockHttpResponse(out)
    ss.init(config)

    assertEquals(0, httpSession.session.size)

    ss.doPost(req, res)

    assertEquals(1, httpSession.session.size)
    val xmlOut = new String(bout.toByteArray)
    assertTrue(xmlOut.indexOf("<price>25</price>") > -1)


    val xmlFire = <batch-execution>
                  <insert out-identifier='something'>
                    <foo.bar.Cheese>
                      <name>stilton</name>
                      <price>25</price>
                    </foo.bar.Cheese>
                  </insert>
                  <fire-all-rules/>   //NOTE THIS !
              </batch-execution>

    val req_ = new MockHttpRequest("/mystatefulrules", new ByteArrayInputStream(xmlFire.toString.getBytes), httpSession)
    val bout_ = new ByteArrayOutputStream
    val out_ = new MockServletOutputStream(bout_)
    val res_ = new MockHttpResponse(out_)



    ss.doPost(req_, res_)

    val xmlOut_ = new String(bout_.toByteArray)
    println(xmlOut_)
    assertTrue(xmlOut_.indexOf("") > -1)

    assertEquals(1, httpSession.session.size)

    ss.doDelete(req_, res_)
    assertTrue(httpSession.disposed)

  }


  def testLogout = {
    val sl = new StatefulListener
    val sess = new MockHttpSession(null)
    sl.sessionDestroyed(new HttpSessionEvent(sess))

  }





}




  class MockServletConfig(ctx: MockServletContext) extends ServletConfig {
    def getInitParameterNames = null

    def getInitParameter(p1: String) = ""

    def getServletName = ""

    def getServletContext = ctx
  }
