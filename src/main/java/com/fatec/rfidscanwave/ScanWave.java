package com.fatec.rfidscanwave;

import com.fatec.rfidscanwave.controller.RFIDController;
import com.fatec.rfidscanwave.view.ScanWaveView;
import com.fatec.rfidscanwave.util.ImageUtil;
import com.fatec.rfidscanwave.util.RFIDManager;
import com.fatec.rfidscanwave.util.StringUtil;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.*;

public class ScanWave extends Application {

    @Override
    public void start(Stage stage) {
        ScanWaveView scanWaveView = new ScanWaveView();

        AnchorPane root = new AnchorPane();
        scanWaveView.createInterface(stage, root);

        Scene scene = new Scene(root, 320 * 3, 240 * 3);

        stage.setTitle("RFID Scan Wave");
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.setResizable(false);
        stage.fullScreenProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(!newValue)
                    stage.setMaximized(true);
            }
        });
        stage.show();
        stage.requestFocus();
        scanWaveView.createRFID(stage, root);
        scanWaveView.verify(scene);
    }

    public static void main(String[] args) throws IOException {
        launch();
    }
}