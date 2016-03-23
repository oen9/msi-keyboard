import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.Date
import java.util.concurrent.TimeUnit
import javafx.application.Application

import org.apache.log4j.Logger
import org.kohsuke.args4j.{CmdLineException, CmdLineParser}
import pl.oen.msi.keyboard._
import pl.oen.usb.UsbDevice
import sun.util.locale.provider.TimeZoneNameUtility

import scala.collection.JavaConversions._

object App {
  val LOGGER: Logger = Logger.getLogger(classOf[App])

  def main2(args: Array[String]) = {

    val colourBuffer: ByteBuffer = ByteBuffer.allocateDirect(8)
    colourBuffer.put(Array[Byte](1, 2, 66, 1, 2, 1, 1, -20))
    val modeBuffer: ByteBuffer = ByteBuffer.allocateDirect(8)
    modeBuffer.put(Array[Byte](1, 2, 65, 1, 0, 0, 0, -20))

    val usbDevice = new UsbDevice(0x1770.toShort, -256.toShort)

    usbDevice.sendToDevice {
      usbDevice.transferData(colourBuffer)
      usbDevice.transferData(modeBuffer)
    }

    usbDevice.close
  }

  def main(args: Array[String]): Unit = {
    val args2: Args = new Args
    val parser: CmdLineParser = new CmdLineParser(args2)

    try {
      parser.parseArgument(args.toSeq)
      if (args2.x) {
        startConsoleApp(args2)
      } else {
        Application.launch(classOf[MainWindow], args:_*)
      }
    } catch {
      case e: CmdLineException =>
        val error: String = e.getMessage + System.lineSeparator
        val baos: ByteArrayOutputStream = new ByteArrayOutputStream
        parser.printUsage(baos)
        LOGGER.error(error + baos.toString)
    }
  }

  def startConsoleApp(args: Args) {
    val keyboardUsbConnector: KeyboardUsbConnector = new KeyboardUsbConnectorImpl2

    try {
      keyboardUsbConnector.setColours(args.colour1, args.colour2, args.colour3)

    } catch {
      case e @ (_: DeviceNotFoundException | _: pl.oen.usb.DeviceNotFoundException) => {
        LOGGER.error("Device not found")
        LOGGER.debug("Device not found", e)

      }  case e: Throwable => {
        LOGGER.error("WTF")
        LOGGER.debug("WTF", e)
      }
    }
  }
}
