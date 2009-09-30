import drools.rex.StatelessServer
import java.io.{ByteArrayOutputStream, ByteArrayInputStream, File}
import java.lang.String
import java.util.HashMap
import javax.servlet.ServletConfig
import junit.framework.TestCase
import junit.framework.Assert._

/**
 * 
 * @author Michael Neale
 */

class StatelessServerTest extends Utils {

  def testPost = {
    val ss = new StatelessServer

    val rl = new File("rule.drl")

    val drl = "package foo.bar\n declare Cheese \n name: String \n price: Integer \n end \n rule 'whee' \n when \n c: Cheese() \n then \n c.setPrice(42); \n System.out.println(42);\n end";
    writeFile(rl, drl)

    val xml = <change-set xmlns='http://drools.org/drools-5.0/change-set' xmlns:xs='http://www.w3.org/2001/XMLSchema-instance' xs:schemaLocation='http://drools.org/drools-5.0/change-set drools-change-set-5.0.xsd' >
                <add>
                  <resource source={rl.toURI.toURL.toString} type='DRL' />
                </add>
              </change-set>

    val cs = new File("myrules.xml")
    writeFile(cs, xml.toString)

    val agentPath = cs.toURI.toURL.toString.replace("/myrules.xml", "")


    val attributes = new HashMap[String, Object]
    val ctx = new MockServletContext(Map("agent-config-directory" -> agentPath, "loop-detection-enabled" -> "false"), attributes)

    val config = new MockServletConfig(ctx)

    val name = "myrules"


    val xmlPost = <batch-execution>
                  <insert out-identifier='something'>
                    <foo.bar.Cheese>
                      <name>stilton</name>
                      <price>25</price>
                    </foo.bar.Cheese>
                  </insert>
              </batch-execution>

    val req = new MockHttpRequest("/myrules", new ByteArrayInputStream(xmlPost.toString.getBytes), null)
    val bout = new ByteArrayOutputStream
    val out = new MockServletOutputStream(bout)
    val res = new MockHttpResponse(out)
    ss.init(config)

    ss.doPost(req, res)

    val xmlOut = new String(bout.toByteArray)
    assertTrue(xmlOut.indexOf("<price>42</price>") > -1)


    val req_ = new MockHttpRequest("/myrules", new ByteArrayInputStream(xmlPost.toString.getBytes), null)
    val bout_ = new ByteArrayOutputStream
    val out_ = new MockServletOutputStream(bout_)
    val res_ = new MockHttpResponse(out_)

    ss.doPost(req_, res_)

    val xmlOut_ = new String(bout_.toByteArray)
    assertTrue(xmlOut_.indexOf("<price>42</price>") > -1)




  }


  class MockServletConfig(ctx: MockServletContext) extends ServletConfig {
    def getInitParameterNames = null

    def getInitParameter(p1: String) = ""

    def getServletName = ""

    def getServletContext = ctx
  }
}