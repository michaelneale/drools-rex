import java.io.File
import java.io.FileOutputStream
import junit.framework.TestCase
import org.drools.agent.KnowledgeAgent
import org.drools.agent.KnowledgeAgentFactory
import org.drools.io.ResourceFactory
import junit.framework.Assert._

/**
 * 
 * @author Michael Neale
 */

class KATest extends Utils {
  def testRules = {
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

    val ka = KnowledgeAgentFactory.newKnowledgeAgent("XX", KnowledgeAgentFactory.newKnowledgeAgentConfiguration)
    ka.applyChangeSet(ResourceFactory.newUrlResource(cs.toURI.toURL))

    val kb = ka.getKnowledgeBase
    assertNotNull(kb)
  }


}