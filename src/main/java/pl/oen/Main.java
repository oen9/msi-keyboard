package pl.oen;

import org.apache.log4j.Logger;
import org.usb4java.*;

import java.nio.ByteBuffer;

public final class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class);

    private Main() {
    }

    public static void main(String... args) {

        byte region = 1;
        byte colour = 1;
        if (2 > args.length) {
            LOGGER.info("arg1: region, arg2: colour");
        } else {
            region = Byte.parseByte(args[0]);
            colour = Byte.parseByte(args[1]);
        }

        LOGGER.info("App started");

        Context context = new Context();
        int result = LibUsb.init(context);
        if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to initialize libusb.", result);

        short vendorId = 0x1770;
        short productId = -256;

        Device device = findDevice(vendorId, productId);

        LOGGER.info(device);


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LibUsb.exit(context);
            LOGGER.info("App closed");
        }));

        DeviceHandle handle = new DeviceHandle();
        int result99 = LibUsb.open(device, handle);
        if (result99 != LibUsb.SUCCESS) throw new LibUsbException("Unable to open USB device", result99);
        try {

            int interfaceNumber = 0;
            LOGGER.info(handle);
            int resutl100 = LibUsb.claimInterface(handle, interfaceNumber);
            if (resutl100 != LibUsb.SUCCESS) throw new LibUsbException("Unable to claim interface", resutl100);
            try {

                ByteBuffer buffer = ByteBuffer.allocateDirect(8);
                buffer.put(new byte[]{1, 2, 66, region, colour, 1, 1, -20});

                int report_number = 1;
                int transfered = LibUsb.controlTransfer(handle,
                        (byte) (LibUsb.REQUEST_TYPE_CLASS | LibUsb.RECIPIENT_INTERFACE | LibUsb.ENDPOINT_OUT),
                        (byte) 0x09,
                        (short) ((3/*HID feature*/ << 8) | report_number),
                        (short) interfaceNumber,
                        buffer,
                        2000);
                if (transfered < 0) {
                    throw new LibUsbException("Control transfer failed", transfered);
                }
                System.out.println(transfered + " bytes sent");


                ByteBuffer buffer2 = ByteBuffer.allocateDirect(8);
                buffer2.put(new byte[]{1, 2, 65, 1, 0, 0, 0, -20});

                int transfered2 = LibUsb.controlTransfer(handle,
                        (byte) (LibUsb.REQUEST_TYPE_CLASS | LibUsb.RECIPIENT_INTERFACE | LibUsb.ENDPOINT_OUT),
                        (byte) 0x09,
                        (short) ((3/*HID feature*/ << 8) | report_number),
                        (short) interfaceNumber,
                        buffer2,
                        2000);
                if (transfered2 < 0) {
                    throw new LibUsbException("Control transfer failed", transfered2);
                }
                System.out.println(transfered2 + " bytes sent");

            } finally {
                resutl100 = LibUsb.releaseInterface(handle, interfaceNumber);
                if (resutl100 != LibUsb.SUCCESS) {
                    throw new LibUsbException("Unable to release interface", resutl100);
                }
            }
        } finally {
            LibUsb.close(handle);
        }

    }

    public static Device findDevice(short vendorId, short productId) {
        // Read the USB device list
        DeviceList list = new DeviceList();
        int result = LibUsb.getDeviceList(null, list);
        if (result < 0) throw new LibUsbException("Unable to get device list", result);

        try {
            // Iterate over all devices and scan for the right one
            for (Device device : list) {
                DeviceDescriptor descriptor = new DeviceDescriptor();
                result = LibUsb.getDeviceDescriptor(device, descriptor);
                if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to read device descriptor", result);
                LOGGER.info(descriptor);
                if (descriptor.idVendor() == vendorId && descriptor.idProduct() == productId) return device;
            }
        } finally {
            // Ensure the allocated device list is freed
            LibUsb.freeDeviceList(list, true);
        }

        // Device not found
        return null;
    }
}
