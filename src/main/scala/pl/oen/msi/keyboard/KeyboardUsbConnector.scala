package pl.oen.msi.keyboard

import java.nio.ByteBuffer

import org.apache.log4j.Logger
import org.usb4java._

class KeyboardUsbConnector {
  private val LOGGER: Logger = Logger.getLogger(classOf[KeyboardUsbConnector])
  protected var vendorId: Short = 0x1770
  protected var productId: Short = -256
  protected var interfaceNumber: Short = 0
  protected var report_number: Int = 1
  protected var modeNormal: Byte = 1

  def setColours(colour1: Option[Byte], colour2: Option[Byte], colour3: Option[Byte]) {
    val keyboardSchema = KeyboardSchema(colour1, colour2, colour3)

    setColour(1.toByte, keyboardSchema.colour1)
    setColour(2.toByte, keyboardSchema.colour2)
    setColour(3.toByte, keyboardSchema.colour3)
  }

  def setColour(region: Byte, colour: Byte) {
    val context: Context = initLibUsb
    val handle: DeviceHandle = createDevicehandle(vendorId, productId)

    try {
      val detach: Boolean = detachKernelDriver(handle, interfaceNumber)
      setColour(region, colour, handle)
      setMode(modeNormal, handle)
      attachKernelDriver(handle, interfaceNumber, detach)
    } finally {
      LibUsb.close(handle)
    }

    LibUsb.exit(context)
  }

  protected def detachKernelDriver(handle: DeviceHandle, interfaceNumber: Int): Boolean = {
    val detach: Boolean = 1 == LibUsb.kernelDriverActive(handle, interfaceNumber)
    if (detach) {
      val result: Int = LibUsb.detachKernelDriver(handle, interfaceNumber)
      if (result != LibUsb.SUCCESS) {
        throw new LibUsbException("Unable to detach kernel driver", result)
      }
    }
    detach
  }

  protected def attachKernelDriver(handle: DeviceHandle, interfaceNumber: Int, detach: Boolean) {
    if (detach) {
      val result: Int = LibUsb.attachKernelDriver(handle, interfaceNumber)
      if (result != LibUsb.SUCCESS) {
        throw new LibUsbException("Unable to re-attach kernel driver", result)
      }
    }
  }

  protected def initLibUsb: Context = {
    val context: Context = new Context
    val result: Int = LibUsb.init(context)
    if (result != LibUsb.SUCCESS) {
      throw new LibUsbException("Unable to initialize libusb.", result)
    }
    context
  }

  protected def createDevicehandle(vendorId: Short, productId: Short): DeviceHandle = {
    val device: Device = findDevice(vendorId, productId)
    if (null == device) {
      throw new DeviceNotFoundException(vendorId, productId)
    }
    val handle: DeviceHandle = new DeviceHandle
    val result: Int = LibUsb.open(device, handle)
    if (result != LibUsb.SUCCESS) {
      throw new LibUsbException("Unable to open USB device", result)
    }
    handle
  }

  protected def setColour(region: Byte, colour: Byte, handle: DeviceHandle) {
    val buffer: ByteBuffer = ByteBuffer.allocateDirect(8)
    buffer.put(Array[Byte](1, 2, 66, region, colour, 1, 1, -20))
    sendToDevice(buffer, handle)
  }

  protected def setMode(mode: Byte, handle: DeviceHandle) {
    val buffer: ByteBuffer = ByteBuffer.allocateDirect(8)
    buffer.put(Array[Byte](1, 2, 65, mode, 0, 0, 0, -20))
    sendToDevice(buffer, handle)
  }

  protected def sendToDevice(buffer: ByteBuffer, handle: DeviceHandle) {
    var result: Int = LibUsb.claimInterface(handle, interfaceNumber)
    if (result != LibUsb.SUCCESS) {
      throw new LibUsbException("Unable to claim interface", result)
    }
    try {
      val transfered: Int = LibUsb.controlTransfer(handle, (LibUsb.REQUEST_TYPE_CLASS | LibUsb.RECIPIENT_INTERFACE | LibUsb.ENDPOINT_OUT).toByte, 0x09.toByte, ((3 << 8) | report_number).toShort, interfaceNumber, buffer, 2000)
      if (transfered < 0) {
        throw new LibUsbException("Control transfer failed", transfered)
      }
      var sent: String = ""
      for (i <- 0 until buffer.capacity()) {
        sent += buffer.get(i) + " "
      }
      LOGGER.info(transfered + " bytes sent: " + sent)
    } finally {
      result = LibUsb.releaseInterface(handle, interfaceNumber)
      if (result != LibUsb.SUCCESS) {
        throw new LibUsbException("Unable to release interface", result)
      }
    }
  }

  protected def findDevice(vendorId: Short, productId: Short): Device = {
    val list: DeviceList = new DeviceList
    var result: Int = LibUsb.getDeviceList(null, list)
    if (result < 0) {
      throw new LibUsbException("Unable to get device list", result)
    }
    try {
      import scala.collection.JavaConversions._
      for (device <- list) {
        val descriptor: DeviceDescriptor = new DeviceDescriptor
        result = LibUsb.getDeviceDescriptor(device, descriptor)
        if (result != LibUsb.SUCCESS) {
          throw new LibUsbException("Unable to read device descriptor", result)
        }
        if (descriptor.idVendor == vendorId && descriptor.idProduct == productId) {
          return device
        }
      }
    } finally {
      LibUsb.freeDeviceList(list, true)
    }
    null
  }
}

class DeviceNotFoundException(vendorId: Short, productId: Short)
  extends RuntimeException("DeviceNotFoundException{" + "vendorId=" + vendorId + ", productId=" + productId + "}")