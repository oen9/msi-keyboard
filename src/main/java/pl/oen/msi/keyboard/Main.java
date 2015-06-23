package pl.oen.msi.keyboard;

import org.apache.log4j.Logger;

public final class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class);

    private Main() {
    }

    public static void main(String... args) {
        byte colour = 1;
        if (1 > args.length) {
            LOGGER.info("arg1: colour");
        } else {
            colour = Byte.parseByte(args[0]);
        }

        KeyboardUsbConnector keyboardUsbConnector = new KeyboardUsbConnector();
        keyboardUsbConnector.setColour((byte)1, colour);
        keyboardUsbConnector.setColour((byte)2, colour);
        keyboardUsbConnector.setColour((byte)3, colour);
    }
}
