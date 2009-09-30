import java.lang.Exception
import java.lang.Throwable
import java.lang.String
import javax.servlet.ServletContext

/**
 * 
 * @author Michael Neale
 */

class MockServletContext(initParams: Map[String, String], attributes: java.util.HashMap[String, Object]) extends ServletContext {

  var calledError = false


  def getContextPath = ""

  def getContext(p1: String) = null

  def getServerInfo = ""

  def getServletContextName = ""

  def getMajorVersion = 0

  def log(p1: String, p2: Throwable) = {
    println(p1)
    this.calledError = true
  }

  def log(p1: Exception, p2: String) = {}

  def getResourceAsStream(p1: String) = null

  def removeAttribute(p1: String) = {}

  def getMinorVersion = 0

  def getMimeType(p1: String) = ""

  def getServlets = null

  def getAttribute(p1: String) = attributes.get(p1) 

  def getServlet(p1: String) = null

  def log(p1: String) = {}

  def getRequestDispatcher(p1: String) = null

  def getNamedDispatcher(p1: String) = null

  def getResourcePaths(p1: String) = null

  def getAttributeNames = null

  def getInitParameterNames = null

  def getRealPath(p1: String) = ""

  def getResource(p1: String) = null

  def setAttribute(p1: String, p2: Object) = {attributes.put(p1, p2)}

  def getInitParameter(p1: String) = initParams.get(p1) match { case Some(x) => x case None => null }

  def getServletNames = null
}