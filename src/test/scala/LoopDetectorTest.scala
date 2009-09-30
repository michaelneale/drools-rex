import drools.rex.LoopDetectorListener
import java.util.{Arrays, Collections}
import junit.framework.TestCase
import junit.framework.TestCase
import junit.framework.Assert._
import scala.util.parsing.combinator.syntactical._
import util.parsing.combinator.lexical.StdLexical

/**
 * 
 * @author Michael Neale
 */
class LoopDetectorTest extends TestCase {

  def testSomething = {
    
    
    val list = Array(2, 1, 6, 4, 2, 1, 6, 4, 2, 1, 6, 4, 2, 1, 6, 4, 2, 1, 6, 5, 7, 5, 4, 3, 8, 7, 6, 6, 6, 8, 3, 4, 10).map(_.toString)
    val (ls, num) = locateLoop(list)
    println(ls.toString)
    assertEquals("4", ls(0))
    assertEquals("6", ls(1))
    assertEquals("1", ls(2))
    assertEquals("2", ls(3))
    println(num)
    assertEquals(19, num)

    val (ls1, num1) = locateLoop(Array("A", "B", "C"))
    println(ls1.toString)
    assertEquals(0, num1)
    println(num1)
    
    val (ls2, num2) = locateLoop(Array("A", "A", "A", "A"))
    println(ls2.toString)
    println(num2)
    assertEquals(1, ls2.length)
    assertEquals(4, num2)
    
    val (ls3, num3) = locateLoop(Array("A", "B", "C", "A", "B", "C", "A", "B", "C"))
    println(ls3.toString)
    println(num3)

    val (ls4, num4) = locateLoop(Array("X", "A", "B", "C", "A", "B", "C", "A", "B", "C"))
    println(ls4.toString)
    println(num4)
    assertEquals(0, num4)

    val (ls5, num5) = locateLoop(Array("X", "A", "A", "A"))
    println(ls5.toString)
    println(num5)
    assertEquals(0, num5)

    val (ls6, num6) = locateLoop(Array("A", "A", "A", "Q", "P"))
    println(ls6.toString)
    assertEquals("A", ls6(0))
    println(num6)
    assertEquals(3, num6)

  }



  def locateLoop(ls: Array[String]) = {
    val newList = new java.util.ArrayList[String]
    var rev = ls.reverse //reversing it as the stupid examples I wrote backwards to how it actually happens. 
    for (s <- rev) newList.add(s)
    val x = new LoopDetectorListener(1000, 250, (msg: String) => println(msg))
    x.locateLoop(newList)

  }





  

}