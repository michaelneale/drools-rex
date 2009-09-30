import java.io.OutputStream
import java.lang.String
import javax.servlet.ServletOutputStream

/**
 * 
 * @author Michael Neale
 */

class MockServletOutputStream(out: OutputStream) extends ServletOutputStream {
  override def flush = {}


  def write(b: Int) = {out.write(b)}

  override def write(b: Array[Byte]) = {out.write(b)}

  override def close = {}

  override def println = {}

  override def print(p1: String) = {}

  override def print(p1: Boolean) = {}

  override def print(p1: Char) = {}

  override def print(p1: Int) = {}

  override def print(p1: Long) = {}

  override def print(p1: Float) = {}

  override def print(p1: Double) = {}

  override def println(p1: Double) = {}

  override def println(p1: Float) = {}

  override def println(p1: Long) = {}

  override def println(p1: String) = {}

  override def println(p1: Boolean) = {}

  override def println(p1: Char) = {}

  override def println(p1: Int) = {}
}