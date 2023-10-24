package com.fatec.rfidscanwave.view;

import com.fatec.rfidscanwave.controller.InterfaceController;
import com.fatec.rfidscanwave.controller.RFIDController;
import com.fatec.rfidscanwave.db.ScanWaveDB;
import com.fatec.rfidscanwave.util.SQLUtil;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class ScanWaveView {
    private ScanWaveDB db;
    private InterfaceController interfaceController;
    private RFIDController rfidController;

    public ScanWaveView(){
        this.db = new ScanWaveDB();
    }

    public boolean reloadDb(){
        try {
            if (db.getDb() == null) {
                db.reloadDb();
            }
        } catch (RuntimeException e){
            e.printStackTrace();
        }

        return db.getDb() == null;
    }

    public void verify(Scene scene){
        if(db.getDb() == null){
            interfaceController.getRoot().setDisable(true);
            new Thread(new SQLUtil(scene, this)).start();
        }
    }

    public void createInterface(Stage stage, Pane root){
        if(interfaceController == null) {
            interfaceController = new InterfaceController(db);
            interfaceController.setParent(this);
            interfaceController.configureInterface(stage, root);
        }
    }
    public void createRFID(Stage stage, Pane root){
        if(rfidController == null) {
            rfidController = new RFIDController(db);
            rfidController.setParent(this);
            rfidController.create(stage, root);
        }
    }

    public ScanWaveDB getDb() {
        return db;
    }

    public InterfaceController getInterface(){
        return interfaceController;
    }

    public RFIDController getRFID(){
        return rfidController;
    }
}