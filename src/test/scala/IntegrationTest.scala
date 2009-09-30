import drools.rex._
import java.io.{ByteArrayInputStream, File}
import java.lang.management.ManagementFactory
import java.util.HashMap
import javax.management.ObjectName
import javax.servlet.http.HttpServletResponse
import junit.framework.TestCase
import junit.framework.Assert._
import org.apache.commons.httpclient.methods.{PostMethod, GetMethod}
import org.apache.commons.httpclient.{NameValuePair, HttpClient}
import org.mortbay.jetty.servlet.{FilterHolder, ServletHolder, Context, ServletHandler}
import org.mortbay.jetty.webapp.WebAppContext
import org.mortbay.jetty.{Server, Connector}
import org.mortbay.jetty.bio.SocketConnector

/**
 *
 * @author Michael Neale
 */

class IntegrationTest extends Utils {
  override def tearDown = {
    try {
      ManagementFactory.getPlatformMBeanServer.unregisterMBean(new ObjectName("org.drools.rex:type=KnowledgeBase,name=/mychangeset.xml"))
    } catch { case e: Exception => println(e.getMessage) }
  }

  def testEndToEnd = {
    //write rules to a given location
    val rl = new File("myrules.drl")
    val drl = "package foo.bar\n declare Cheese \n name: String \n price: Integer \n end \n rule 'whee' \n when \n c: Cheese(price==6) \n Cheese(price==5) \n then \n c.setPrice(42); \n System.out.println(42);\n end";
    writeFile(rl, drl)


    val changeSetFile = new File("mychangeset.xml")
    val changeset = <change-set xmlns='http://drools.org/drools-5.0/change-set' xmlns:xs='http://www.w3.org/2001/XMLSchema-instance' xs:schemaLocation='http://drools.org/drools-5.0/change-set drools-change-set-5.0.xsd'>
    <add>
    <resource source={rl.toURI.toURL.toString} type='DRL'/>
    </add>
    </change-set>
    writeFile(changeSetFile, changeset.toString)

    val configDir = changeSetFile.toURI.toURL.toString.replace("/mychangeset.xml", "")
    println(configDir)


    val server = new Server(8888);

    val ctx = new Context(server, "/", Context.SESSIONS)

    println(configDir)
    ctx.setInitParams(getMap(configDir))
    ctx.addEventListener(new AgentListener)
    ctx.addServlet(new ServletHolder(new StatelessServer), "/stateless/*")
    ctx.addServlet(new ServletHolder(new StatefulServer), "/stateful/*")
    ctx.addServlet(new ServletHolder(new Status), "/")
    ctx.addEventListener(new StatefulListener)

    server.setStopAtShutdown(true)
    server.start



    //now invoke...via HTTP
    val client = new HttpClient

    val insert = new PostMethod("http://localhost:8888/stateful/mychangeset")
    val initialXML = <batch-execution> <insert out-identifier='foo1'>
      <foo.bar.Cheese>
      <name>cheddar</name>
      <price>5</price>
      </foo.bar.Cheese>
      </insert><fire-all-rules/></batch-execution>
    insert.setRequestBody(new ByteArrayInputStream(initialXML.toString.getBytes))

    client.executeMethod(insert)
    val response = insert.getResponseBodyAsString
    assertFalse(response.indexOf("<price>42</price>") > -1)

    assertEquals(1, client.getState.getCookies().length)
    val cookie = client.getState.getCookies()(0).toString

    val insert2 = new PostMethod("http://localhost:8888/stateful/mychangeset")
    insert2.setRequestHeader("SOAPAction", "whatever...")
    val initialXML2 = <SOAP-ENV:Envelope
      xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"
      SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
      <SOAP-ENV:Body><batch-execution> <insert out-identifier='foo1'>
      <foo.bar.Cheese>
      <name>cheddar</name>
      <price>6</price>
      </foo.bar.Cheese>
      </insert><fire-all-rules/></batch-execution></SOAP-ENV:Body>
    </SOAP-ENV:Envelope>
    insert2.setRequestBody(new ByteArrayInputStream(initialXML2.toString.getBytes))

    client.executeMethod(insert2)
    val resp = insert2.getResponseBodyAsString
    assertTrue(resp.indexOf("<price>42</price>") > -1)
    assertTrue(resp.indexOf("SOAP-ENV") > -1)


    assertEquals(1, client.getState.getCookies().length)
    assertEquals(cookie, client.getState.getCookies()(0).toString)


    val dispose = new PostMethod("http://localhost:8888/stateful/mychangeset/dispose-session")
    client.executeMethod(dispose)

    assertEquals(HttpServletResponse.SC_ACCEPTED, dispose.getStatusCode)


    val status = new GetMethod("http://localhost:8888/")
    client.executeMethod(status)
    assertTrue(status.getResponseBodyAsString.indexOf("OK") > -1)


    val get = new GetMethod("http://localhost:8888/stateful/mychangeset?wsdl")
    client.executeMethod(get)
    assertTrue(get.getResponseBodyAsString.indexOf("<definitions") == 0)

    val get_ = new GetMethod("http://localhost:8888/stateless/mychangeset?wsdl")
    client.executeMethod(get_)
    assertNotNull(get_.getResponseBodyAsString)
    assertTrue(get_.getResponseBodyAsString.indexOf("<definitions") == 0)


    server stop
  }


  def testStateless = {

    //write rules to a given location
    val rl = new File("myrules.drl")
    val drl = "package foo.bar\n declare Cheese \n name: String \n price: Integer \n end \n rule 'whee' \n when \n c: Cheese(price==6) \n then \n c.setPrice(42); \n System.out.println(42);\n end";
    writeFile(rl, drl)


    val changeSetFile = new File("mychangeset.xml")
    val changeset = <change-set xmlns='http://drools.org/drools-5.0/change-set' xmlns:xs='http://www.w3.org/2001/XMLSchema-instance' xs:schemaLocation='http://drools.org/drools-5.0/change-set drools-change-set-5.0.xsd'>
    <add>
    <resource source={rl.toURI.toURL.toString} type='DRL'/>
    </add>
    </change-set>
    writeFile(changeSetFile, changeset.toString)

    val configDir = changeSetFile.toURI.toURL.toString.replace("/mychangeset.xml", "")
    println(configDir)


    val server = new Server(8888);

    val ctx = new Context(server, "/", Context.SESSIONS)


    ctx.setInitParams(getMap(configDir))
    ctx.addEventListener(new AgentListener)
    ctx.addServlet(new ServletHolder(new StatelessServer), "/stateless/*")
    ctx.addServlet(new ServletHolder(new StatefulServer), "/stateful/*")
    ctx.addServlet(new ServletHolder(new Status), "/")
    ctx.addEventListener(new StatefulListener)

    server.setStopAtShutdown(true)
    server.start


    //now invoke...via HTTP
    val client = new HttpClient

    val insert = new PostMethod("http://localhost:8888/stateless/mychangeset")
    val initialXML = <batch-execution> <insert out-identifier='foo1'>
      <foo.bar.Cheese>
      <name>cheddar</name>
      <price>6</price>
      </foo.bar.Cheese>
      </insert><fire-all-rules/></batch-execution>
    insert.setRequestBody(new ByteArrayInputStream(initialXML.toString.getBytes))

    client.executeMethod(insert)
    val response = insert.getResponseBodyAsString
    assertTrue(response.indexOf("<price>42</price>") > -1)

    //now via SOAP
    val insert_ = new PostMethod("http://localhost:8888/stateless/mychangeset")
    val initialXML_ = <SOAP-ENV:Envelope
      xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"
      SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
      <SOAP-ENV:Body><batch-execution> <insert out-identifier='foo1'>
      <foo.bar.Cheese>
      <name>cheddar</name>
      <price>6</price>
      </foo.bar.Cheese>
      </insert><fire-all-rules/></batch-execution></SOAP-ENV:Body>
    </SOAP-ENV:Envelope>
    insert_.setRequestHeader("SOAPAction", "http://foo.bar")
    insert_.setRequestBody(new ByteArrayInputStream(initialXML_.toString.getBytes))

    client.executeMethod(insert_)
    val response_ = insert_.getResponseBodyAsString
    assertTrue(response_.indexOf("<price>42</price>") > -1)



    server stop
  }



  def testInfiniteLoop = {
    val rl = new File("myrules.drl")

    val drl = "package foo.bar\n declare Cheese \n name: String \n price: Integer \n end \n rule 'ping' \n when \n c: Cheese(price==6) \n then \n c.setPrice(42); \n update(c);\n end" +
            "\nrule 'pong' \n when \n c: Cheese(price==42) \n then \n c.setPrice(6); \n update(c); \n end";



    writeFile(rl, drl)

    val changeSetFile = new File("mychangeset.xml")
    val changeset = <change-set xmlns='http://drools.org/drools-5.0/change-set' xmlns:xs='http://www.w3.org/2001/XMLSchema-instance' xs:schemaLocation='http://drools.org/drools-5.0/change-set drools-change-set-5.0.xsd'>
    <add>
    <resource source={rl.toURI.toURL.toString} type='DRL'/>
    </add>
    </change-set>
    writeFile(changeSetFile, changeset.toString)

    val configDir = changeSetFile.toURI.toURL.toString.replace("/mychangeset.xml", "")
    println(configDir)


    val server = new Server(8888);

    val ctx = new Context(server, "/", Context.SESSIONS)


    ctx.setInitParams(getMap(configDir))
    ctx.addEventListener(new AgentListener)
    ctx.addServlet(new ServletHolder(new StatelessServer), "/stateless/*")
    ctx.addServlet(new ServletHolder(new StatefulServer), "/stateful/*")
    ctx.addServlet(new ServletHolder(new Status), "/")
    ctx.addEventListener(new StatefulListener)

    server.setStopAtShutdown(true)
    server.start


    //now invoke...via HTTP
    val client = new HttpClient

    val insert = new PostMethod("http://localhost:8888/stateless/mychangeset")
    val initialXML = <batch-execution> <insert out-identifier='foo1'>
      <foo.bar.Cheese>
      <name>cheddar</name>
      <price>6</price>
      </foo.bar.Cheese>
      </insert><fire-all-rules/></batch-execution>
    insert.setRequestBody(new ByteArrayInputStream(initialXML.toString.getBytes))

    client.executeMethod(insert)
    val response = insert.getResponseBodyAsString
    assertTrue(response.indexOf("</price>") > -1)

    //now via SOAP
    val insert_ = new PostMethod("http://localhost:8888/stateless/mychangeset")
    val initialXML_ = <SOAP-ENV:Envelope
      xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"
      SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
      <SOAP-ENV:Body><batch-execution> <insert out-identifier='foo1'>
      <foo.bar.Cheese>
      <name>cheddar</name>
      <price>6</price>
      </foo.bar.Cheese>
      </insert><fire-all-rules/></batch-execution></SOAP-ENV:Body>
    </SOAP-ENV:Envelope>
    insert_.setRequestHeader("SOAPAction", "http://foo.bar")
    insert_.setRequestBody(new ByteArrayInputStream(initialXML_.toString.getBytes))

    client.executeMethod(insert_)
    val response_ = insert_.getResponseBodyAsString
    assertTrue(response_.indexOf("</price>") > -1) //don't really care what the value is...



    server stop
  }


  def testMissingDependency = {
    //write rules to a given location
    val rl = new File("myrules.drl")
    val drl = "package foo.bar\n import missing.dep.Cheese \n rule 'whee' \n when \n c: Cheese(price==6) \n Cheese(price==5) \n then \n c.setPrice(42); \n System.out.println(42);\n end";
    writeFile(rl, drl)


    val changeSetFile = new File("mychangeset.xml")
    val changeset = <change-set xmlns='http://drools.org/drools-5.0/change-set' xmlns:xs='http://www.w3.org/2001/XMLSchema-instance' xs:schemaLocation='http://drools.org/drools-5.0/change-set drools-change-set-5.0.xsd'>
    <add>
    <resource source={rl.toURI.toURL.toString} type='DRL'/>
    </add>
    </change-set>
    writeFile(changeSetFile, changeset.toString)

    val configDir = changeSetFile.toURI.toURL.toString.replace("/mychangeset.xml", "")
    println(configDir)


    val server = new Server(8888);

    val ctx = new Context(server, "/", Context.SESSIONS)

    println(configDir)
    ctx.setInitParams(getMap(configDir))
    ctx.addEventListener(new AgentListener)
    ctx.addServlet(new ServletHolder(new StatelessServer), "/stateless/*")
    ctx.addServlet(new ServletHolder(new StatefulServer), "/stateful/*")
    ctx.addServlet(new ServletHolder(new Status), "/")
    ctx.addEventListener(new StatefulListener)

    server.setStopAtShutdown(true)
    server.start


    //now invoke...via HTTP
    val client = new HttpClient

    val insert = new PostMethod("http://localhost:8888/stateful/mychangeset")
    val initialXML = <batch-execution> <insert out-identifier='foo1'>
      <missing.dep.Cheese>
      <name>cheddar</name>
      <price>5</price>
      </missing.dep.Cheese>
      </insert><fire-all-rules/></batch-execution>
    insert.setRequestBody(new ByteArrayInputStream(initialXML.toString.getBytes))

    client.executeMethod(insert)
    val response = insert.getResponseBodyAsString
    println(response)
    assertFalse(response.indexOf("<price>42</price>") > -1)

    server stop

  }




  def getMap(configDir: String) = {
    val hm = new HashMap[String, String]
    hm.put("agent-config-directory", configDir)
    hm.put("loop-detection-enabled", "true")
    hm
  }

}