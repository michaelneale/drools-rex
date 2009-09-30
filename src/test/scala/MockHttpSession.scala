import java.lang.String
import java.util.HashMap
import javax.servlet.http.HttpSession
import javax.servlet.ServletContext

/**
 * 
 * @author Michael Neale
 */

class MockHttpSession(ctx: ServletContext) extends HttpSession {

  val session = new java.util.HashMap[String, Object]

  var disposed = false

  def getCreationTime = 0L

  def removeAttribute(p1: String) = {}

  def invalidate = {disposed = true}

  def getLastAccessedTime = 0L

  def removeValue(p1: String) = {}

  def getId = ""

  def getAttribute(p1: String) = session.get(p1)

  def getValue(p1: String) = null

  def setMaxInactiveInterval(p1: Int) = {}

  def getValueNames = null

  def putValue(p1: String, p2: Any) = {}

  def getMaxInactiveInterval = 0

  def getSessionContext = null

  def isNew = false

  def getAttributeNames = null

  def setAttribute(p1: String, p2: Object) = {session.put(p1, p2)}

  def getServletContext = ctx
}