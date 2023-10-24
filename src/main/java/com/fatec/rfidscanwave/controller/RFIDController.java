package com.fatec.rfidscanwave.controller;

import com.fatec.rfidscanwave.ScanWave;
import com.fatec.rfidscanwave.db.ScanWaveDB;
import com.fatec.rfidscanwave.model.Clock;
import com.fatec.rfidscanwave.util.*;
import com.fatec.rfidscanwave.view.ScanWaveView;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class RFIDController {
    private ScanWaveView parent;
    private final ScanWaveDB db;
    private final TextField rfidField;
    private final RFIDManager rfidManager;
    private boolean processingRFID = false;
    private RFIDCommand rfidCommand = null;

    public RFIDController(ScanWaveDB db){
        this.db = db;
        this.rfidField = new TextField();
        this.rfidManager = new RFIDManager(rfidField);
    }

    public void create(Stage stage, Pane root){
        stage.setOnShowing(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                rfidField.requestFocus();
            }
        });

        rfidField.setStyle("-fx-opacity: 0;");

        configureRFIDField();

        root.getChildren().add(rfidField);
    }

    private void configureRFIDField(){
        rfidField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                System.out.println("recebeu");
                if(oldValue.isEmpty()) {
                    if(!rfidManager.isThreading()) {
                        Thread thread = new Thread(rfidManager);
                        thread.start();
                    }
                }

                if(processingRFID && rfidCommand == null)
                    return;

                if(newValue.length() == 10 && StringUtil.isNumeric(newValue)){
                    if(rfidManager.isThreading())
                        rfidManager.threading(false);
                    System.out.println("processou");

                    processRFID(newValue);
                    Platform.runLater(rfidField::clear);
                }
            }
        });

    }

    public void processRFID(String rfid){
        int id = db.getIdByRFID(rfid);

        if(rfidCommand == null) {
            setProcessingRFID(true);

            if (id > 0) {
                if (db.canClock(id)) {
                    db.clock(id);
                    parent.getInterface().action(id, InterfaceCommand.Command.DISPLAY_USER);
                } else {
                    if(db.getLastClock(id).getState() == Clock.ClockState.CLOCK_IN) {
                        parent.getInterface().action(id, InterfaceCommand.Command.ALREADY_CLOCKED);
                    } else {
                        parent.getInterface().action(id, InterfaceCommand.Command.CANT_WORK);
                    }
                }
            } else {
                parent.getInterface().action(id, InterfaceCommand.Command.WRONG_USER);
            }
        } else if(rfidCommand.getId() == id){
            rfidCommand.appendChecked();
            parent.getInterface().action(id, InterfaceCommand.Command.FORCE_CLOCK_OUT);
        }
    }

    public void setProcessingRFID(boolean processingRFID) {
        this.processingRFID = processingRFID;
    }

    public TextField getRfidField() {
        return rfidField;
    }

    public void prepareCommand(int id, RFIDCommand.Command command){
        if(rfidCommand == null)
            rfidCommand = new RFIDCommand(id, command);
    }

    public RFIDCommand getRfidCommand() {
        return rfidCommand;
    }

    public void finishCommand(){
        rfidCommand = null;
    }

    public void setParent(ScanWaveView parent) {
        if(this.parent == null)
            this.parent = parent;
    }
}
