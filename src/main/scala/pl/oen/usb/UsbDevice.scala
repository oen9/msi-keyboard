package pl.oen.usb

import java.nio.ByteBuffer

import org.usb4java._
import pl.oen.Log
import scala.collection.JavaConversions._

class UsbDevice(val usbDeviceIds: UsbDeviceIds, val interfaceNumber: Short = 0) extends Log {
  def this(vendorId: Short, productId: Short) {
    this(UsbDeviceIds(vendorId, productId))
  }

  var detach = false
  val report_number = 1
  val context: Context = initLibUsb
  val handle: DeviceHandle = createDevicehandle

  def sendToDevice(usbCommunication: => Unit) {
    this.synchronized {
      detachKernelDriver

      val claimResult = LibUsb.claimInterface(handle, interfaceNumber)
      if (claimResult != LibUsb.SUCCESS) throw new LibUsbException("Unable to claim interface", claimResult)

      try {
        usbCommunication

      } finally {
        val releaseResult = LibUsb.releaseInterface(handle, interfaceNumber)
        if (releaseResult != LibUsb.SUCCESS) LOGGER.error(new LibUsbException("Unable to release interface", releaseResult)) //throw new LibUsbException("Unable to release interface", releaseResult)

        attachKernelDriver.foreach(LOGGER.error(_))
      }
    }
  }

  def transferData(buffer: ByteBuffer) = {
      val transfered: Int = LibUsb.controlTransfer(handle,
        (LibUsb.REQUEST_TYPE_CLASS | LibUsb.RECIPIENT_INTERFACE | LibUsb.ENDPOINT_OUT).toByte,
        0x09.toByte, ((3 << 8) | report_number).toShort,
        interfaceNumber,
        buffer,
        2000)

      if (transfered < 0) throw new LibUsbException("Control transfer failed", transfered)

      var sent: String = ""
      for (i <- 0 until buffer.capacity()) sent += buffer.get(i) + " "
      LOGGER.info(transfered + " bytes sent: " + sent)
  }

  def close() {
    LibUsb.exit(context)
  }

  protected def initLibUsb: Context = {
    val context: Context = new Context

    val result = LibUsb.init(context)
    if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to initialize libusb.", result)

    context
  }

  protected def createDevicehandle = {
    val device = findDevice.getOrElse(throw new DeviceNotFoundException(usbDeviceIds))

    val handle: DeviceHandle = new DeviceHandle
    val result = LibUsb.open(device, handle)
    if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to open USB device", result)
    handle
  }

  protected def findDevice: Option[Device] = {
    val deviceList = new DeviceList
    val deviceListResult = LibUsb.getDeviceList(null, deviceList)
    if (deviceListResult < 0) throw new LibUsbException("Unable to get device list", deviceListResult)

    try {
      for (device <- deviceList) {
        val descriptor: DeviceDescriptor = new DeviceDescriptor
        val descriptorResult = LibUsb.getDeviceDescriptor(device, descriptor)
        if (descriptorResult != LibUsb.SUCCESS) throw new LibUsbException("Unable to read device descriptor", descriptorResult)

        val nextDeviceIds = UsbDeviceIds(descriptor.idVendor, descriptor.idProduct)
        if (usbDeviceIds == nextDeviceIds) return Some(device)
      }

    } finally {
      LibUsb.freeDeviceList(deviceList, true)
    }
    None
  }

  protected def detachKernelDriver {
    detach = 1 == LibUsb.kernelDriverActive(handle, interfaceNumber)
    if (detach) {
      val result: Int = LibUsb.detachKernelDriver(handle, interfaceNumber)
      if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to detach kernel driver", result)
    }
  }

  protected def attachKernelDriver : Option[LibUsbException] = {
    if (detach) {
      val result: Int = LibUsb.attachKernelDriver(handle, interfaceNumber)
      if (result != LibUsb.SUCCESS) return Some(new LibUsbException("Unable to re-attach kernel driver", result))
    }
    None
  }
}

class DeviceNotFoundException(usbDeviceIds: UsbDeviceIds)
  extends RuntimeException("vendorId=" + usbDeviceIds.vendorId + ", productId=" + usbDeviceIds.productId)