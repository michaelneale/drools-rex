import drools.rex.KnowledgeSessions
import junit.framework.TestCase
import junit.framework.Assert._
import org.drools.runtime.StatefulKnowledgeSession

/**
 * 
 * @author Michael Neale
 */

class KnowledgeSessionsTest extends Utils {
  def testSession = {
    val ka = getDefaultKA
    val httpSess = new MockHttpSession(new MockServletContext(Map("loop-detection-enabled" -> "false"), null))
    val sess = KnowledgeSessions.getStatefulSession(ka, httpSess)
    sess match {
      case Some(x: StatefulKnowledgeSession) => {
        assertEquals(1, x.getKnowledgeBase.getKnowledgePackages.size)
        assertEquals(1, httpSess.session.size)
        KnowledgeSessions.getStatefulSession(ka, httpSess) match {
          case Some(x2 : StatefulKnowledgeSession) => {
            assertSame(x, x2)
            assertEquals(1, httpSess.session.size)
          }
          case None => fail("should return some")
        }
      }
      case None => fail("should return some")
    }

  }
}