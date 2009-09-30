package drools.rex

import java.io.InputStream
import javax.servlet.ServletContext
import org.apache.poi.util.IOUtils
import org.drools.event.rule._



import org.drools.KnowledgeBase
import org.drools.runtime.help.BatchExecutionHelper
import org.drools.runtime.pipeline.{ResultHandler, PipelineFactory, Pipeline}
import org.drools.runtime.{StatefulKnowledgeSession, StatelessKnowledgeSession}
/**
 * 
 * @author Michael Neale
 */
object SessionExecutor {



  def execute(xml: InputStream, ksession: Either[StatefulKnowledgeSession, StatelessKnowledgeSession], soap: Boolean) : String = {
       val rh = new Result
       val pipe = makePipeline(ksession)
       if (!soap) {
          pipe.insert(xml, rh)
          rh.result.asInstanceOf[String]
       } else {
          pipe.insert(soapUnwrap(new String(IOUtils.toByteArray(xml))), rh)
          soapWrap(rh.result.asInstanceOf[String])
       }
  }


  def makePipeline(ksession: Either[StatefulKnowledgeSession, StatelessKnowledgeSession]) : Pipeline = {
        val executeResultHandler = PipelineFactory.newExecuteResultHandler

        val assignResult = PipelineFactory.newAssignObjectAsResult
        assignResult.setReceiver( executeResultHandler )


        val outTransformer = PipelineFactory.newXStreamToXmlTransformer(BatchExecutionHelper.newXStreamMarshaller())
        outTransformer setReceiver( assignResult )

        val batchExecution = PipelineFactory.newCommandExecutor
        batchExecution.setReceiver( outTransformer )

        val inTransformer = PipelineFactory.newXStreamFromXmlTransformer( BatchExecutionHelper.newXStreamMarshaller )
        inTransformer setReceiver( batchExecution )

        ksession match {
          case Left(stateful) => {
            val pipeline = PipelineFactory.newStatefulKnowledgeSessionPipeline( stateful )
            pipeline setReceiver( inTransformer )
            pipeline
          }
          case Right(stateless) => {
            val pipeline = PipelineFactory.newStatelessKnowledgeSessionPipeline( stateless )
            pipeline setReceiver( inTransformer )
            pipeline
          }
        }
  }


  def soapWrap(xml: String) : String = {
    """<SOAP-ENV:Envelope
      xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"
      SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
      <SOAP-ENV:Body>""" + xml + """
      </SOAP-ENV:Body>
    </SOAP-ENV:Envelope>"""
  }

  def soapUnwrap(soap: String) : String = {
    soap.substring( soap.indexOf("<SOAP-ENV:Body>") + 15, soap.indexOf("</SOAP-ENV:Body>"))
  }



  class Result(var result: Object) extends ResultHandler {
        def handleResult(ob: Object) = {
          this.result = ob
        }
  }



  
}



/**
 * For attempting to trap infinte loops and put the kaibosh on them. Whatever kaibosh means.
 * Uses cycle detection to help this, and some other basic rules of thumb.
 * Not recommended for use with rules that use deep recursion.
 */
class LoopDetectorListener(firingsOfInterest: Int, loopSize: Int, logger: (String => Unit)) extends AgendaEventListener {

  var currentCount = 0;
  var firingTrail = new java.util.ArrayList[String](firingsOfInterest)

  def afterActivationFired(event: AfterActivationFiredEvent) = {
    currentCount = currentCount + 1
    if (currentCount > firingsOfInterest) {
      //val x = event.getActivation.asInstanceOf[AgendaItem] // this is how we get the hashCode to see if data has changed
      //println(event.getActivation.getRule.getName + " " + x.getTuple.asInstanceOf[LeftTuple].hashCode)
      
      if (currentCount % firingsOfInterest == 0) {
        val (loop, number) = locateLoop(firingTrail)
        val repetitions = if (loop.length > 0) number / loop.length else 0
        if (repetitions >= loopSize) {
          //we have a loop
          logger(currentCount + " firings of rule names: [" + loop.foldLeft("")(_ + " " + _) + "]")
          event.getKnowledgeRuntime.halt
        }

        //reset things
        firingTrail = new java.util.ArrayList[String]
      } else {
        firingTrail.add(event.getActivation.getRule.getName)
      }
    }
  }


  def activationCancelled(event: ActivationCancelledEvent) = {}

  def agendaGroupPopped(event: AgendaGroupPoppedEvent) = {}

  def beforeActivationFired(event: BeforeActivationFiredEvent) = {}

  def activationCreated(event: ActivationCreatedEvent) = {}

  def agendaGroupPushed(event: AgendaGroupPushedEvent) = {}


  /**
   * Take a list, return a list of the pattern that is repeated, and the total number of items that are
   * detected to be in a repeated pattern.  Loosely based on the tortoise and hare algorithm, only in this case
   * I break the tortoise legs as I don't want him to leave the start line.
   */
  def locateLoop(list: java.util.List[String]) : (Array[String], Int) = {
                 var i = 1;

    val endVal = inv(list,0)
    var pattern = Array(endVal)
    var cycleSuspected = false
    var confirmed = false
    var cycleY = 0
    var cycleX = 0
    while (i < list.size - 1 && !confirmed) {
        val v = inv(list, i)
        if (v == endVal) {
          cycleSuspected = true
          cycleX = 0
          cycleY = i
          var repeatingItems = Array(endVal)
          while (cycleSuspected) {
            cycleX = cycleX + 1
            cycleY = cycleY + 1
            if (cycleY < list.size) {
              if (inv(list,cycleX) != inv(list,cycleY)) {
                if (cycleX >= i) {
                  pattern = repeatingItems
                 // println("Terminated With Loop Found")
                }
                cycleSuspected = false
              } else {
                // println(list(cycleX - 1))
                if (cycleX == i) {
                  confirmed = true
                 // println("Found !")
                }
                if (!confirmed) {
                  repeatingItems = repeatingItems ++ Array(inv(list,cycleX))
                }
              }
            } else {
              pattern = repeatingItems
              cycleSuspected = false
            }
          }
        }
        i = i + 1
      }

      (pattern.reverse, cycleY)

  }

  def inv(a: java.util.List[String], i: Int) = {
    a.get(a.size - i - 1)
  }



}





