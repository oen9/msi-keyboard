package pl.oen.msi.keyboard;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import pl.oen.msi.keyboard.exception.DeviceNotFoundException;

import java.io.ByteArrayOutputStream;

public final class Main  {
    private static final Logger LOGGER = Logger.getLogger(Main.class);

    public static void main(String... args) {
        Args args2 = new Args();
        CmdLineParser parser = new CmdLineParser(args2);
        try {
            parser.parseArgument(args);
            if (args2.isX()) {
                startConsoleApp(args2);
            } else {
//                MainWindow.launch(MainWindow.class, args);
                TestApplication.launch(TestApplication.class, args);
            }
        } catch (CmdLineException e) {
            String error = e.getMessage() + System.lineSeparator();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            parser.printUsage(baos);
            LOGGER.error(error + baos.toString());
        }
    }

    protected static void startConsoleApp(final Args args) {
        KeyboardUsbConnector keyboardUsbConnector = new KeyboardUsbConnector();

        try {
            keyboardUsbConnector.setColours(args.getColour1(), args.getColour2(), args.getColour3());
        } catch (DeviceNotFoundException e) {
            LOGGER.error("Device not found");
            LOGGER.debug("Device not found", e);
        }
    }
}
