package pl.oen.msi.keyboard

import java.nio.ByteBuffer

import pl.oen.Log
import pl.oen.usb.UsbDevice

import scala.collection.JavaConversions._

class KeyboardUsbConnectorImpl2 extends KeyboardUsbConnector with Log {
  protected val vendorId: Short = 0x1770
  protected val productId: Short = -256

  protected val modeNormal: Byte = 1
  protected val modeBuffer = prepareModeBuffer(modeNormal)

  def setColours(colour1: Option[Byte], colour2: Option[Byte], colour3: Option[Byte]) {
    val keyboardSchema = KeyboardSchema(colour1, colour2, colour3)

    val usbDevice = new UsbDevice(vendorId, productId)

    setColour(1.toByte, keyboardSchema.colour1, usbDevice)
    setColour(2.toByte, keyboardSchema.colour2, usbDevice)
    setColour(3.toByte, keyboardSchema.colour3, usbDevice)

    usbDevice.close
  }

  protected def setColour(region: Byte, colour: Byte, usbDevice: UsbDevice) {
    val colourBuffer = prepareColourBuffer(region, colour)

    usbDevice.sendToDevice {
      usbDevice.transferData(colourBuffer)
      usbDevice.transferData(modeBuffer)
    }
  }

  protected def prepareColourBuffer(region: Byte, colour: Byte) = {
    val buffer: ByteBuffer = ByteBuffer.allocateDirect(8)
    buffer.put(Array[Byte](1, 2, 66, region, colour, 1, 1, -20))
    buffer
  }

  protected def prepareModeBuffer(mode: Byte) = {
    val buffer: ByteBuffer = ByteBuffer.allocateDirect(8)
    buffer.put(Array[Byte](1, 2, 65, mode, 0, 0, 0, -20))
    buffer
  }
}
