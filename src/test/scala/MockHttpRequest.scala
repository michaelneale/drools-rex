import java.io.InputStream
import java.lang.String
import java.util.HashMap
import javax.servlet.http.HttpServletRequest
import javax.servlet.ServletInputStream

/**
 * 
 * @author Michael Neale
 */

class MockHttpRequest(pathInfo: String, in: InputStream, httpSession: MockHttpSession) extends HttpServletRequest {
  def getScheme = ""

  def getLocalPort = 0

  def getLocalName = ""

  def getRemotePort = 0

  def getLocalAddr = ""

  def getContentType = ""

  def getParameterMap = null

  def getRemoteAddr = ""

  def removeAttribute(p1: String) = {}

  def getParameter(p1: String) = ""

  def getParameterNames = null

  def isSecure = false

  def getParameterValues(p1: String) = null

  def getServerName = ""

  def getLocale = null

  def getRequestDispatcher(p1: String) = null

  def getRealPath(p1: String) = ""

  def setAttribute(p1: String, p2: Any) = {}

  def getLocales = null

  def getReader = null

  def getRemoteHost = ""

  def getInputStream = new MockStream(in)

  def getAttribute(p1: String) = null

  def getProtocol = ""

  def getCharacterEncoding = ""

  def getContentLength = 0

  def getServerPort = 0

  def getAttributeNames = null

  def setCharacterEncoding(p1: String) = {}

  def getServletPath = ""

  def getHeaderNames = null

  def getMethod = ""

  def getSession = httpSession

  def getRemoteUser = ""

  def getRequestedSessionId = ""

  def isRequestedSessionIdValid = false

  def getHeaders(p1: String) = null

  def isRequestedSessionIdFromCookie = false

  def getContextPath = ""

  def getRequestURI = ""

  def getQueryString = ""

  def getHeader(p1: String) = null

  def getPathInfo = pathInfo

  def getSession(p1: Boolean) = null

  def isRequestedSessionIdFromURL = false

  def getRequestURL = null

  def getPathTranslated = ""

  def getUserPrincipal = null

  def isRequestedSessionIdFromUrl = false

  def getCookies = null

  def getIntHeader(p1: String) = 0

  def getDateHeader(p1: String) = 0L

  def isUserInRole(p1: String) = false

  def getAuthType = ""
}


class MockStream(in: InputStream) extends ServletInputStream {
  override def read = in.read

  override def mark(readlimit: Int) = {in.mark(readlimit)}

  override def markSupported = in.markSupported

  override def available = in.available

  override def close = in.close

  override def reset = in.reset

  override def read(b: Array[Byte], off: Int, len: Int) = in.read(b, off, len)

  override def skip(n: Long) = in.skip(n)

  override def read(b: Array[Byte]) = in.read(b)
}