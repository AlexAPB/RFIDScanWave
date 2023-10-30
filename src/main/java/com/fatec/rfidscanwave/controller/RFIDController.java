package com.fatec.rfidscanwave.controller;

import com.fatec.rfidscanwave.db.ScanWaveDB;
import com.fatec.rfidscanwave.model.EmployeeModel;
import com.fatec.rfidscanwave.model.clock.ClockDayModel;
import com.fatec.rfidscanwave.model.clock.ClockModel;
import com.fatec.rfidscanwave.util.*;
import com.fatec.rfidscanwave.view.ScanWaveView;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static com.fatec.rfidscanwave.model.clock.ClockModel.ClockState.*;

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

                    processRFID(newValue);
                    Platform.runLater(rfidField::clear);
                }
            }
        });

    }

    public void processRFID(String rfid){
        int id = db.getIdByRFID(rfid);
        InterfaceCommand.Command command = null;
        EmployeeModel employee = db.getEmployee(id);
        ClockDayModel lastClock = employee.getClockList().get(employee.getClockList().size() - 1);

        setProcessingRFID(true);

        ClockModel nextClock = new ClockModel();
        nextClock.setState(UNDEFINED);

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime localClockIn = lastClock.getClockIn() == null ? null : LocalDateTime.of(
                lastClock.getClockIn().getDate(),
                employee.getShift().getClockInTime()
        );

        LocalDateTime localLunchOut = localClockIn == null ? null : LocalDateTime.from(localClockIn)
                .plus(employee.getShift().getWorkdayDuration().dividedBy(2))
                .minusSeconds(employee.getShift().getBreakDuration().toSecondOfDay() / 2);

        LocalDateTime localLunchReturn = localClockIn == null ? null : LocalDateTime.from(localClockIn)
                .plus(employee.getShift().getWorkdayDuration().dividedBy(2))
                .plusSeconds(employee.getShift().getBreakDuration().toSecondOfDay() / 2);

        LocalDateTime localClockOut = localClockIn == null ? null : LocalDateTime
                .from(localClockIn)
                .plus(employee.getShift().getWorkdayDuration());

        switch (lastClock.getLastState()){
            case CLOCK_IN -> {
                //Hora de All Mossar
                if(Duration.between(localLunchOut, now).toSeconds() > 0 && Duration.between(localLunchReturn, now).toSeconds() < 0){
                    command = InterfaceCommand.Command.LUNCH_OUT;
                    nextClock.setDate(LocalDate.now());
                    nextClock.setTime(LocalTime.now());
                    nextClock.setState(LUNCH_OUT);
                }
                //Hora de ir Enn Bora
                else if(Duration.between(localClockOut, now).toSeconds() > 0){
                    command = InterfaceCommand.Command.DISPLAY_USER;

                    nextClock.setDate(LocalDate.now());
                    nextClock.setTime(LocalTime.now());
                    nextClock.setState(CLOCK_OUT);
                }
                //Forçar Bater ponto
                else {
                    if (rfidCommand == null) {
                        command = InterfaceCommand.Command.ALREADY_CLOCKED;
                    } else {
                        if (rfidCommand.getId() != id) {
                            return;
                        }
                        command = InterfaceCommand.Command.FORCE_CLOCK_OUT;
                        getRfidCommand().appendChecked();

                        if (rfidCommand.remainTimes() <= 0) {
                            nextClock.setDate(LocalDate.now());
                            nextClock.setTime(LocalTime.now());
                            nextClock.setState(CLOCK_OUT);
                        }
                    }
                }
            }
            case LUNCH_OUT -> {
                //Voltar do Au Mosso (10 min de tolerancia para a volta do All Mosso, senao força o Clock Out)
                if(Duration.between(localLunchReturn, now).toSeconds() < 600){
                    command = InterfaceCommand.Command.LUNCH_RETURN;

                    nextClock.setDate(LocalDate.now());
                    nextClock.setTime(LocalTime.now());
                    nextClock.setState(LUNCH_RETURN);
                }
                //Hora de ir Enn Bora se o filho da ptua não tiver batido o ponto da volta
                else if(Duration.between(localClockOut, now).toSeconds() > 0){
                    command = InterfaceCommand.Command.DISPLAY_USER;

                    nextClock.setDate(LocalDate.now());
                    nextClock.setTime(LocalTime.now());
                    nextClock.setState(CLOCK_OUT);
                }
                //Forçar Bater ponto
                else {
                    if (rfidCommand == null) {
                        command = InterfaceCommand.Command.ALREADY_CLOCKED;
                    } else {
                        if (rfidCommand.getId() != id) {
                            return;
                        }
                        command = InterfaceCommand.Command.FORCE_CLOCK_OUT;
                        getRfidCommand().appendChecked();

                        if (rfidCommand.remainTimes() <= 0) {
                            nextClock.setDate(LocalDate.now());
                            nextClock.setTime(LocalTime.now());
                            nextClock.setState(CLOCK_OUT);
                        }
                    }
                }
            }
            case LUNCH_RETURN -> {
                //Hora de ir Enn Bora se o filho da ptua não tiver batido o ponto da volta
                if(Duration.between(localClockOut, now).toSeconds() > 0){
                    nextClock.setDate(LocalDate.now());
                    nextClock.setTime(LocalTime.now());

                    nextClock.setDate(LocalDate.now());
                    nextClock.setTime(LocalTime.now());
                    nextClock.setState(CLOCK_OUT);

                    command = InterfaceCommand.Command.DISPLAY_USER;
                }
                //Forçar Bater ponto
                else {
                    if (rfidCommand == null) {
                        command = InterfaceCommand.Command.ALREADY_CLOCKED;
                    } else {
                        if (rfidCommand.getId() != id) {
                            return;
                        }

                        command = InterfaceCommand.Command.FORCE_CLOCK_OUT;
                        getRfidCommand().appendChecked();

                        if (rfidCommand.remainTimes() <= 0) {
                            nextClock.setDate(LocalDate.now());
                            nextClock.setTime(LocalTime.now());
                            nextClock.setState(CLOCK_OUT);
                        }
                    }
                }
            }
            case CLOCK_OUT -> {
                //Faz menos de 11 horas que o carinha trampou
                if(Duration.between(localClockOut, now).toHours() < 11){
                    command = InterfaceCommand.Command.CANT_WORK;
                } else if(Duration.between(localClockIn, now).toHours() <= -2){ //O cara tá tentando trampar 2h mais cedo, wtf
                    command = InterfaceCommand.Command.OFF_PERIOD;
                } else {
                    nextClock.setDate(LocalDate.now());
                    nextClock.setTime(LocalTime.now());
                    nextClock.setState(CLOCK_IN);

                    command = InterfaceCommand.Command.DISPLAY_USER;
                }
            }
            case OFF_DUTY -> {
                command = InterfaceCommand.Command.IN_OFF_DUTY;
            }
            case UNDEFINED -> {
                if (id == -1){
                    command = InterfaceCommand.Command.WRONG_USER;
                } else {
                    command = InterfaceCommand.Command.DISPLAY_USER;
                    nextClock.setDate(LocalDate.now());
                    nextClock.setTime(LocalTime.now());
                    nextClock.setState(CLOCK_IN);
                }
            }
        }

        if(nextClock.getState() == CLOCK_IN){
            System.out.println("Entrando com clock in!");
            db.clock(id, employee, nextClock);
        } else if(nextClock.getState() != UNDEFINED){
            db.updateClock(id, nextClock);
        }

        parent.getInterface().action(id, db.getEmployee(id), command);
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
