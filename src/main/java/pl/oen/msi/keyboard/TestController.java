package pl.oen.msi.keyboard;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import pl.oen.msi.keyboard.exception.DeviceNotFoundException;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class TestController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(TestController.class);

    @FXML
    protected Button changeColoursButton;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        changeColoursButton.setOnAction(event -> {
            KeyboardUsbConnector keyboardUsbConnector = new KeyboardUsbConnector();
            try {
                keyboardUsbConnector.setColours(
                        (byte) 6,
                        (byte) 6,
                        (byte) 6);
            } catch (DeviceNotFoundException e) {
                LOGGER.error("Device not found");
                LOGGER.debug("Device not found", e);
            }
        });
    }
}
