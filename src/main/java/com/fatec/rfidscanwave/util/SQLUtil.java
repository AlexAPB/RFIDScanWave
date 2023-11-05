package com.fatec.rfidscanwave.util;

import com.fatec.rfidscanwave.view.ScanWaveView;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class SQLUtil implements Runnable {
    private ScanWaveView view;
    private Scene scene;

    public SQLUtil(Scene scene, ScanWaveView view) {
        this.scene = scene;
        this.view = view;

        AnchorPane anchorPane = new AnchorPane();

        Label label = new Label("Aguardando conexão!");
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-font-size: 40px; -fx-text-align: center;");
        label.setTextFill(Color.BLACK);

        AnchorPane.setTopAnchor(label, 0D);
        AnchorPane.setRightAnchor(label, 0D);
        AnchorPane.setLeftAnchor(label, 0D);
        AnchorPane.setBottomAnchor(label, 0D);

        anchorPane.getChildren().add(label);

        scene.setRoot(anchorPane);
    }

    @Override
    public void run() {
        while(view.reloadDb()){
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        scene.setRoot(view.getInterface().getRoot());
        view.getInterface().getRoot().setDisable(false);
    }
}
