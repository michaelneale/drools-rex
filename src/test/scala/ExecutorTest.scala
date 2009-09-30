import drools.rex.SessionExecutor
import java.io.{ByteArrayInputStream, File, FileOutputStream}
import java.util.HashMap
import junit.framework.TestCase
import junit.framework.Assert._


import org.drools.agent.KnowledgeAgentFactory
import org.drools.io.ResourceFactory
import org.drools.KnowledgeBase
import scala.actors.Actor
import scala.actors.Actor._

/**
 * 
 * @author Michael Neale
 */

class ExecutorTest extends Utils {

  var counter = 0

  def testExecutron = {
         val ac = actor {
           loop {
             receive {
               case (x: String) => {
                 println("start " + x)
                 Thread.sleep(100)
                 println("awake " + x)
                 counter = counter + 1
                 reply { "returning " + x }
               }
               
             }
           }
         }


        //val q = ac !? (1000, "42")
        //println(q)
        //println("continuing")
        //ac !? (1000,  "43")
        //ac !? (1000, "44")

        val t = new Thread(new Runnable {
           def run = {
            println("HEY:" + (ac !? "hey") + " counter " + counter)
           }
        })
    val t2 = new Thread(new Runnable {
       def run = {
         println("HEY2:" + (ac !? "hey2") + " counter " + counter)
       }
    })
    val t3 = new Thread(new Runnable {
       def run = {
         println("HEY3:" + (ac !? "hey3") + " counter " + counter)
       }
    })

        t.start
        t2.start
        t3.start


        Thread.sleep(2000)
        ""
    
  }

  def testExecutorStatless = {
    val session = getDefaultKB newStatelessKnowledgeSession

    val xml = <batch-execution>
        <insert>
          <foo.bar.Cheese>
            <name>stilton</name>
            <price>25</price>
          </foo.bar.Cheese>
        </insert>
        </batch-execution>
    val res = SessionExecutor.execute(new ByteArrayInputStream(xml.toString.getBytes), Right(session), false)
    println(res)
    assertEquals("<execution-results/>", res)


    val xml_ =
    <SOAP-ENV:Envelope
      xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"
      SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
      <SOAP-ENV:Body>
        <batch-execution>
        <insert>
          <foo.bar.Cheese>
            <name>stilton</name>
            <price>25</price>
          </foo.bar.Cheese>
        </insert>
        </batch-execution>
      </SOAP-ENV:Body>
    </SOAP-ENV:Envelope>
    val res_ = SessionExecutor.execute(new ByteArrayInputStream(xml_.toString.getBytes), Right(session), true)
    assertTrue(res_.indexOf("execution-results")> -1)
    assertTrue(res_.indexOf("SOAP-ENV")> -1)
    println(res_)
  }

  def testExecutorStateful = {
    val session = getDefaultKB newStatefulKnowledgeSession

    val xml = <batch-execution>
        <insert>
          <foo.bar.Cheese>
            <name>stilton</name>
            <price>25</price>
          </foo.bar.Cheese>
        </insert>
        </batch-execution>
    val res = SessionExecutor.execute(new ByteArrayInputStream(xml.toString.getBytes), Left(session), false)
    assertNotNull(res)
    println(res)

    val xml_ = <batch-execution><insert out-identifier='outsomething'>
                <foo.bar.Cheese>
                  <name>cheddar</name>
                  <price>5</price>
                </foo.bar.Cheese>
               </insert><fire-all-rules/></batch-execution>

    val res_ = SessionExecutor.execute(new ByteArrayInputStream(xml_.toString.getBytes), Left(session), false)
    assertNotNull(res_)
    assertTrue(res_.indexOf("<price>42</price>") > -1)
    println(res_)


  }


  def testSOAPWrap = {
    val xml = <hey>ho</hey>.toString
    val xmlOut = SessionExecutor.soapWrap(xml)
    assertTrue(xmlOut.indexOf("SOAP-ENV") > -1)
    assertTrue(xmlOut.indexOf("<hey>ho") > -1)
  }

  def testSOAPUnwrap = {
      val xml =  """<SOAP-ENV:Envelope
      xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"
      SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
      <SOAP-ENV:Body><request>here</request></SOAP-ENV:Body>
    </SOAP-ENV:Envelope>"""
    val res = SessionExecutor.soapUnwrap(xml)
    assertEquals(-1, res.indexOf("SOAP"))
    assertTrue(res.indexOf("<request>here</request>") > -1)
  }





}