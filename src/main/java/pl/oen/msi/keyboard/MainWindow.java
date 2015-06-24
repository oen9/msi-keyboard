package pl.oen.msi.keyboard;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import pl.oen.msi.keyboard.exception.DeviceNotFoundException;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.prefs.Preferences;

public class MainWindow extends Application {
    private static final Logger LOGGER = Logger.getLogger(MainWindow.class);

    protected Preferences preferences = Preferences.userNodeForPackage(MainWindow.class);

    public void start(final Stage primaryStage) throws Exception {
        primaryStage.setTitle("MSI KEYBOARD");

        Circle circle = new Circle(200, 200, 80, Color.DARKRED);

        TextField colour1 = new TextField(preferences.get("colour1", "6"));
        TextField colour2 = new TextField(preferences.get("colour2", "6"));
        TextField colour3 = new TextField(preferences.get("colour3", "6"));

        Button btn = new Button();
        btn.setText("Change colours!");
        btn.setOnAction(event -> {
            KeyboardUsbConnector keyboardUsbConnector = new KeyboardUsbConnector();
            try {
                keyboardUsbConnector.setColours(
                        Byte.valueOf(colour1.getText()),
                        Byte.valueOf(colour2.getText()),
                        Byte.valueOf(colour3.getText()));

                preferences.put("colour1", colour1.getText());
                preferences.put("colour2", colour2.getText());
                preferences.put("colour3", colour3.getText());

            } catch (DeviceNotFoundException e) {
                LOGGER.error("Device not found");
                LOGGER.debug("Device not found", e);
            }
        });

        StackPane root = new StackPane();
        VBox vBox = new VBox();
        root.getChildren().add(circle);
        root.getChildren().add(vBox);
//        root.setStyle("-fx-background: #123456;");
        Image image = new Image(getClass().getResourceAsStream("/GS60.jpg"));
        BackgroundImage bi = new BackgroundImage(image, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        root.setBackground(new Background(bi));

        vBox.getChildren().add(btn);
        vBox.getChildren().add(colour1);
        Slider slider = createSlider(colour1);
        vBox.getChildren().addAll(slider);

        vBox.getChildren().add(colour2);
        Slider slider2 = createSlider(colour2);
        vBox.getChildren().addAll(slider2);

        vBox.getChildren().add(colour3);
        Slider slider3 = createSlider(colour3);
        vBox.getChildren().addAll(slider3);

        vBox.setAlignment(Pos.CENTER);
        vBox.setFillWidth(true);

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);

        if (!getParameters().getUnnamed().contains(Args.HIDE_PARAM_NAME)) {
            primaryStage.show();
        }

        Platform.setImplicitExit(false);
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        createTrayIcon(primaryStage);
    }

    protected Slider createSlider(final TextField colour) {
        Slider slider = new Slider(-15, 15, 6);
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            colour.setText(String.valueOf(newValue.byteValue()));
        });

        return slider;
    }

    public void createTrayIcon(final Stage stage) {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();

            java.awt.Image image;
            Dimension traySize = tray.getTrayIconSize();
            image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/keyboard.png")).getScaledInstance(traySize.width - 2, traySize.height, 0);

            TrayIcon trayIcon = new TrayIcon(image);
            trayIcon.addMouseListener(new TrayMouseListener(stage));

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                LOGGER.error("Error while init tray-icon", e);
            }
        }
    }

    protected class TrayMouseListener implements MouseListener {
        protected Stage stage;

        public TrayMouseListener(final Stage stage) {
            this.stage = stage;
        }

        @Override
        public void mouseClicked(final MouseEvent e) {
            Platform.runLater(() -> {
                if (stage.isShowing()) {
                    stage.hide();
                } else {
                    stage.show();
                }
            });
        }

        @Override
        public void mousePressed(final MouseEvent e) {
        }

        @Override
        public void mouseReleased(final MouseEvent e) {
        }

        @Override
        public void mouseEntered(final MouseEvent e) {
        }

        @Override
        public void mouseExited(final MouseEvent e) {
        }
    }
}