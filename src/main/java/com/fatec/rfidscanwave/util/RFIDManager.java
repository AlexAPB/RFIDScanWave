package com.fatec.rfidscanwave.util;

import javafx.application.Platform;
import javafx.scene.control.TextField;

public class RFIDManager implements Runnable {
    private final TextField rfidField;
    private long time;
    private boolean isThreading = false;

    public RFIDManager(TextField rfidField){
        this.rfidField = rfidField;
    }

    public void threading(boolean isThreading){
        this.isThreading = isThreading;
    }

    public void updateTime(){
        time = System.currentTimeMillis();
    }

    public boolean isThreading() {
        return isThreading;
    }


    @Override
    public void run() {
        isThreading = true;
        time = System.currentTimeMillis();

        while(isThreading){
            if(System.currentTimeMillis() - time > 100){
                Platform.runLater(rfidField::clear);
                break;
            }
        }
        isThreading = false;
    }
}
