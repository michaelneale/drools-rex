import java.lang.String
import java.util.Locale
import javax.servlet.http.{Cookie, HttpServletResponse}
/**
 * 
 * @author Michael Neale
 */

class MockHttpResponse(out: MockServletOutputStream) extends HttpServletResponse {
  def setCharacterEncoding(p1: String) = {}


  def addCookie(p1: Cookie) = {}

  def getContentType = ""

  var sendErrorCalled = false

  def getOutputStream = out

  def setBufferSize(p1: Int) = {}

  def getBufferSize = 0

  def setLocale(p1: Locale) = {}

  def setContentType(p1: String) = {}

  def setContentLength(p1: Int) = {}

  def flushBuffer = {}

  def reset = {}

  def resetBuffer = {}

  def getLocale = null

  def getCharacterEncoding = ""

  def isCommitted = false

  def getWriter = null


  def setStatus(p1: Int, p2: String) = {}

  def addHeader(p1: String, p2: String) = {}

  def containsHeader(p1: String) = false

  def encodeRedirectUrl(p1: String) = ""

  def setDateHeader(p1: String, p2: Long) = {}

  def setIntHeader(p1: String, p2: Int) = {}

  def sendError(p1: Int, p2: String) = {
    sendErrorCalled = true

  }

  def encodeUrl(p1: String) = ""

  def sendError(p1: Int) = {}

  def encodeURL(p1: String) = ""

  def sendRedirect(p1: String) = {}

  def setStatus(p1: Int) = {}

  def addDateHeader(p1: String, p2: Long) = {}

  def encodeRedirectURL(p1: String) = ""

  def setHeader(p1: String, p2: String) = {}

  def addIntHeader(p1: String, p2: Int) = {}
}