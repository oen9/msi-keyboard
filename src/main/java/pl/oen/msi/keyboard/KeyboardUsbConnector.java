package pl.oen.msi.keyboard;

import org.apache.log4j.Logger;
import org.usb4java.*;
import pl.oen.msi.keyboard.exception.DeviceNotFoundException;

import java.nio.ByteBuffer;

public class KeyboardUsbConnector {
    private static final Logger LOGGER = Logger.getLogger(KeyboardUsbConnector.class);
    protected short vendorId = 0x1770;
    protected short productId = -256;
    protected short interfaceNumber = 0;
    protected int report_number = 1;
    protected byte modeNormal = 1;

    public void setColours(final Byte colour1, final Byte colour2, final Byte colour3) {
        setColour((byte) 1, colour1);
        setColour((byte) 2, null != colour2 ? colour2 : colour1);
        setColour((byte) 3, null != colour3 ? colour3 : colour1);
    }

    public void setColour(final byte region, final byte colour) {
        Context context = initLibUsb();

        DeviceHandle handle = createDevicehandle(vendorId, productId);

        try {
            boolean detach = detachKernelDriver(handle, interfaceNumber);

            setColour(region, colour, handle);
            setMode(modeNormal, handle);

            attachKernelDriver(handle, interfaceNumber, detach);
        } finally {
            LibUsb.close(handle);
        }

        LibUsb.exit(context);
    }

    protected boolean detachKernelDriver(final DeviceHandle handle, final int interfaceNumber) {
        boolean detach = 1 == LibUsb.kernelDriverActive(handle, interfaceNumber);

        if (detach) {
            int result = LibUsb.detachKernelDriver(handle, interfaceNumber);
            if (result != LibUsb.SUCCESS) {
                throw new LibUsbException("Unable to detach kernel driver", result);
            }
        }

        return detach;
    }

    protected void attachKernelDriver(final DeviceHandle handle, final int interfaceNumber, final boolean detach) {
        if (detach) {
            int result = LibUsb.attachKernelDriver(handle, interfaceNumber);
            if (result != LibUsb.SUCCESS) {
                throw new LibUsbException("Unable to re-attach kernel driver", result);
            }
        }
    }

    protected Context initLibUsb() {
        Context context = new Context();
        int result = LibUsb.init(context);
        if (result != LibUsb.SUCCESS) {
            throw new LibUsbException("Unable to initialize libusb.", result);
        }

        return context;
    }

    protected DeviceHandle createDevicehandle(final short vendorId, final short productId) {
        Device device = findDevice(vendorId, productId);

        if (null == device) {
            throw new DeviceNotFoundException(vendorId, productId);
        }

        DeviceHandle handle = new DeviceHandle();
        int result = LibUsb.open(device, handle);
        if (result != LibUsb.SUCCESS) {
            throw new LibUsbException("Unable to open USB device", result);
        }

        return handle;
    }

    protected void setColour(final byte region, final byte colour, final DeviceHandle handle) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(8);
        buffer.put(new byte[]{1, 2, 66, region, colour, 1, 1, -20});
        sendToDevice(buffer, handle);
    }

    protected void setMode(final byte mode, final DeviceHandle handle) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(8);
        buffer.put(new byte[]{1, 2, 65, mode, 0, 0, 0, -20});
        sendToDevice(buffer, handle);
    }

    protected void sendToDevice(final ByteBuffer buffer, final DeviceHandle handle) {
        int result = LibUsb.claimInterface(handle, interfaceNumber);
        if (result != LibUsb.SUCCESS) {
            throw new LibUsbException("Unable to claim interface", result);
        }

        try {
            int transfered = LibUsb.controlTransfer(handle,
                    (byte) (LibUsb.REQUEST_TYPE_CLASS | LibUsb.RECIPIENT_INTERFACE | LibUsb.ENDPOINT_OUT),
                    (byte) 0x09,
                    (short) ((3/*HID feature*/ << 8) | report_number),
                    interfaceNumber,
                    buffer,
                    2000);
            if (transfered < 0) {
                throw new LibUsbException("Control transfer failed", transfered);
            }

            String sent = "";
            for (int i = 0; i < buffer.capacity(); i++) {
                sent += buffer.get(i) + " ";
            }
            LOGGER.info(transfered + " bytes sent: " + sent);

        } finally {
            result = LibUsb.releaseInterface(handle, interfaceNumber);
            if (result != LibUsb.SUCCESS) {
                throw new LibUsbException("Unable to release interface", result);
            }
        }
    }

    protected Device findDevice(short vendorId, short productId) {
        // Read the USB device list
        DeviceList list = new DeviceList();
        int result = LibUsb.getDeviceList(null, list);
        if (result < 0) {
            throw new LibUsbException("Unable to get device list", result);
        }

        try {
            // Iterate over all devices and scan for the right one
            for (Device device : list) {
                DeviceDescriptor descriptor = new DeviceDescriptor();
                result = LibUsb.getDeviceDescriptor(device, descriptor);
                if (result != LibUsb.SUCCESS) {
                    throw new LibUsbException("Unable to read device descriptor", result);
                }

                if (descriptor.idVendor() == vendorId && descriptor.idProduct() == productId) {
                    return device;
                }
            }
        } finally {
            // Ensure the allocated device list is freed
            LibUsb.freeDeviceList(list, true);
        }

        // Device not found
        return null;
    }
}
