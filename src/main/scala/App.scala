import java.io.ByteArrayOutputStream
import javafx.application.Application

import org.apache.log4j.Logger
import org.kohsuke.args4j.{CmdLineException, CmdLineParser}
import org.usb4java.LibUsb
import pl.oen.msi.keyboard.{Args, DeviceNotFoundException, KeyboardUsbConnector, MainWindow}
import pl.oen.usb.UsbDevice

import scala.collection.JavaConversions._

object App {
  val LOGGER: Logger = Logger.getLogger(classOf[App])

  def main(args: Array[String]) = {
    val usbDevice = new UsbDevice(0x1770.toShort, -256.toShort)
    usbDevice.close
  }

  def main2(args: Array[String]): Unit = {

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
    val keyboardUsbConnector: KeyboardUsbConnector = new KeyboardUsbConnector
    try {
      keyboardUsbConnector.setColours(args.colour1, args.colour2, args.colour3)
    } catch {
      case e: DeviceNotFoundException => {
        LOGGER.error("Device not found")
        LOGGER.debug("Device not found", e)
      }
    }
  }
}
