package com.fatec.rfidscanwave.controller;

import com.fatec.rfidscanwave.ScanWave;
import com.fatec.rfidscanwave.db.ScanWaveDB;
import com.fatec.rfidscanwave.model.Clock;
import com.fatec.rfidscanwave.model.ClockDay;
import com.fatec.rfidscanwave.model.EmployeeModel;
import com.fatec.rfidscanwave.util.ImageUtil;
import com.fatec.rfidscanwave.util.InterfaceCommand;
import com.fatec.rfidscanwave.util.RFIDCommand;
import com.fatec.rfidscanwave.util.TimeUtil;
import com.fatec.rfidscanwave.view.ScanWaveView;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.fatec.rfidscanwave.model.Clock.ClockState.CLOCK_IN;
import static com.fatec.rfidscanwave.model.Clock.ClockState.CLOCK_OUT;

public class InterfaceController {
    private ScanWaveView parent;
    private InterfaceCommand interfaceCommand;
    private final ScanWaveDB db;
    private AnchorPane root;
    private final VBox interfaceContainer;
    private final HBox dotsBox;
    private final ImageView userImage;
    private final List<Circle> dotsList;
    private final Label message;
    private final Label name;
    private final Label career;
    private final Label clockIn;
    private final Label clockOut;
    private final Label workedHours;
    private final VBox clockBox;

    public InterfaceController(ScanWaveDB db){
        this.db = db;
        this.interfaceContainer = new VBox();
        this.dotsBox = new HBox();
        this.userImage = new ImageView();
        this.message = new Label();
        this.name = new Label();
        this.career = new Label();
        this.clockIn = new Label();
        this.clockOut = new Label();
        this.workedHours = new Label();
        this.clockBox = new VBox();
        this.dotsList = new ArrayList<>();
    }

    public boolean prepareCommand(int id, InterfaceCommand.Command command){
        if(interfaceCommand == null) {
            interfaceCommand = new InterfaceCommand(id, command);
            return true;
        }

        return false;
    }

    public void finishCommand(){
        interfaceCommand = null;
    }

    public void action(int id, InterfaceCommand.Command command){
        Transition transition = null;
        boolean resetImage = false;

        switch (command){
            case DISPLAY_USER -> {
                //When you finish rotating, user information will start loading and showing
                resetImage = true;
                transition = new ClockAnimation().animateClock(
                        new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                displayUser(true);
                                loadUserInformation(id, db.getLastClockDay(id));
                            }
                        }
                );
            }
            case WRONG_USER -> {
                transition = new ClockAnimation().wrongUser();
            }
            case CANT_WORK -> {
                name.setText("Impossível bater ponto!");
                career.setText("É necessário um tempo de " + db.getMinToWork(db.getWorkdayDuration(id)) + " horas entre suas jornadas de trabalho!");
                clockOut.setText("Último ponto: " + db.getLastClock(id).getClock().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")));
                transition = new ClockAnimation().cantWork(name,career, clockOut, clockBox);
            }
            case ALREADY_CLOCKED -> {
                parent.getRFID().prepareCommand(id, RFIDCommand.Command.CLOCK_OUT);
                name.setText("Você já bateu o ponto há " + TimeUtil.getTimeFromSeconds(db.getLastClockDifferenceInSeconds(id)) + "!");
                career.setText("Passe o cartão mais " + parent.getRFID().getRfidCommand().remainTimes() + "x para encerrar forçadamente o seu turno!");

                transition = new ClockAnimation().alreadyClocked(name,career);
            }
            case FORCE_CLOCK_OUT -> {
                if(parent.getRFID().getRfidCommand().remainTimes() > 0) {
                    career.setText("Passe o cartão mais " + parent.getRFID().getRfidCommand().remainTimes() + "x para encerrar forçadamente o seu turno!");
                } else {
                    db.clock(parent.getRFID().getRfidCommand().getId());
                    name.setText("Turno encerrado!");
                    loadClockInformation(db.getLastClockDay(parent.getRFID().getRfidCommand().getId()));
                    displayForAlreadyClock();
                    parent.getRFID().finishCommand();

                    transition = new ClockAnimation().forceClockOut();
                }
            }
        }

        if(transition != null) {
            ClockAnimation.current = transition;
            boolean finalResetImage = resetImage;
            transition.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    parent.getRFID().setProcessingRFID(false);
                    parent.getRFID().finishCommand();
                    resetUserInformation(finalResetImage);
                    displayUser(false);
                }
            });
            transition.play();
        }
    }


    private void displayUser(boolean display) {
        name.setVisible(display);
        message.setVisible(display);
        career.setVisible(display);
        clockIn.setVisible(display);
        clockOut.setVisible(display);
        workedHours.setVisible(display);
        clockBox.setVisible(display);

        for(Circle c : dotsList)
            c.setVisible(!display);
    }

    private RotateTransition getImageRotation(){
        RotateTransition rotate = new RotateTransition(Duration.millis(500), userImage);
        rotate.setFromAngle(0);
        rotate.setToAngle(360);
        return rotate;
    }

    private void fadeContainer(){
        FadeTransition fade = new FadeTransition(Duration.millis(500), interfaceContainer);
        interfaceContainer.setOpacity(0f);
        fade.setFromValue(0f);
        fade.setToValue(1f);
        fade.play();
    }

    public void displayForAlreadyClock(){
        name.setVisible(true);
        career.setVisible(false);
        clockIn.setVisible(true);
        clockOut.setVisible(true);
        workedHours.setVisible(true);
        clockBox.setVisible(true);
    }

    private void resetUserInformation(boolean updateImage){
        name.setText("");
        career.setText("");
        message.setText("");
        clockIn.setText("");
        clockOut.setText("");
        workedHours.setText("");

        if(updateImage) {
            userImage.setImage(new Image(ScanWave.class.getResource("/images/user.png").toString()));
            userImage.setFitHeight(250);
            userImage.setFitWidth(250);
        }
    }

    private void loadUserInformation(int id, ClockDay clockDay){
        EmployeeModel employee = db.getEmployee(id);
        name.setText(employee.getName());
        career.setText(employee.getCareer());
        message.setText(Clock.ClockState.getGreetings(clockDay.getClockOut() == null ? CLOCK_IN : CLOCK_OUT));

        loadClockInformation(clockDay);

        Image user = db.getUserImageById(id);
        if(user != null) {
            userImage.setImage(
                    ImageUtil.getRoundImage(
                            user,
                            (int) (Math.min(user.getWidth(), user.getHeight()) / 2)
                    )
            );
        }
    }

    private void loadClockInformation(ClockDay clockDay){
        clockIn.setText("Início: " + clockDay.getClockIn().clockToHourMinute());
        if(clockDay.getClockIn() != null && clockDay.getClockOut() != null) {
            clockOut.setText("Término: " + clockDay.getClockOut().clockToHourMinute());
            workedHours.setText("Tempo trabalhado: " + clockDay.difference());
        }
    }

    public AnchorPane getRoot() {
        return root;
    }

    public void configureInterface(Stage stage, Pane root){
        this.root = (AnchorPane) root;

        //Configure the root element
        root.setBackground(
                new Background(
                        new BackgroundImage(
                                new Image(Objects.requireNonNull(ScanWave.class.getResource("/images/background.jpg")).toString()),
                                BackgroundRepeat.REPEAT,
                                BackgroundRepeat.REPEAT,
                                BackgroundPosition.CENTER,
                                BackgroundSize.DEFAULT
                        )
                )
        );

        //Configure the interfaceContainer
        interfaceContainer.setFillWidth(true);
        interfaceContainer.setAlignment(Pos.CENTER);

        AnchorPane.setLeftAnchor(interfaceContainer, 0D);
        AnchorPane.setRightAnchor(interfaceContainer, 0D);
        AnchorPane.setBottomAnchor(interfaceContainer, 0D);
        AnchorPane.setTopAnchor(interfaceContainer, 0D);

        //Configure the dotsBox
        dotsBox.setFillHeight(false);
        generateDots();

        //Configure userImage
        userImage.setImage(new Image(ScanWave.class.getResource("/images/user.png").toString()));
        userImage.setFitHeight(250);
        userImage.setFitWidth(250);

        //Set a padding with a Pane
        Pane littleSpace1 = new Pane();
        littleSpace1.setPrefWidth(1);
        littleSpace1.setPrefHeight(10);

        //Configure Name
        name.setStyle("-fx-font-size: 40px; -fx-text-align: center;");
        name.setTextFill(Color.WHITE);
        name.setAlignment(Pos.CENTER);

        //Configure Career
        career.setStyle("-fx-font-size: 25px; -fx-font-style: italic;");
        career.setTextFill(new Color(1, 1, 1, 0.75f));

        //Configure Message
        message.setStyle("-fx-font-size: 70px; -fx-font-weight: bold; -fx-font-family: \"Calibri\", Arial, sans-serif;");
        message.setTextFill(Color.WHITE);

        //Set a padding with a Pane
        Pane littleSpace2 = new Pane();
        littleSpace2.setPrefWidth(1);
        littleSpace2.setPrefHeight(30);

        //Configure Clock In
        clockIn.setStyle("-fx-font-size: 25px; -fx-font-family: \"Segoe UI\", Cambria, \"Trebuchet MS\", Arial, sans-serif;");
        clockIn.setTextFill(Color.WHITE);
        clockIn.setOpacity(0.7);

        //Configure Clock Out
        clockOut.setStyle("-fx-font-size: 25px; -fx-font-family: \"Segoe UI\", Cambria, \"Trebuchet MS\", Arial, sans-serif;");
        clockOut.setTextFill(Color.WHITE);
        clockOut.setOpacity(0.7);

        //Configure Worked Hours
        workedHours.setStyle("-fx-font-size: 25px; -fx-font-family: \"Segoe UI\", Cambria, \"Trebuchet MS\", Arial, sans-serif;");
        workedHours.setTextFill(Color.WHITE);
        workedHours.setOpacity(0.7);

        //Container for CSS
        clockBox.setAlignment(Pos.CENTER);
        clockBox.setFillWidth(false);
        clockBox.setStyle("-fx-border-color: rgba(255, 255, 255, 0.2); -fx-border-width: 2px; -fx-border-radius: 20px; -fx-padding: 10px;");
        clockBox.setPrefWidth(400);

        clockBox.getChildren().add(clockIn);
        clockBox.getChildren().add(clockOut);
        clockBox.getChildren().add(workedHours);

        VBox containerClockBox = new VBox(clockBox);
        containerClockBox.setFillWidth(false);
        containerClockBox.setAlignment(Pos.CENTER);

        //Append the components to the root
        interfaceContainer.getChildren().add(userImage);
        interfaceContainer.getChildren().add(littleSpace1);
        interfaceContainer.getChildren().add(name);
        interfaceContainer.getChildren().add(career);
        interfaceContainer.getChildren().add(dotsBox);
        interfaceContainer.getChildren().add(message);
        interfaceContainer.getChildren().add(littleSpace2);
        interfaceContainer.getChildren().add(containerClockBox);
        root.getChildren().add(interfaceContainer);

        //Hide user's info
        displayUser(false);
    }

    public void generateDots(){
        dotsBox.setAlignment(Pos.CENTER);

        double size = 25;

        for(int i = 0; i < 3; i++){
            Circle circle = new Circle();
            circle.setRadius(size / 2 * 1.75);
            circle.setFill(Color.WHITE);

            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(800), circle);
            scaleTransition.setToX(1.75);
            scaleTransition.setToY(1.75);
            scaleTransition.setAutoReverse(true);
            scaleTransition.setCycleCount(Animation.INDEFINITE);
            scaleTransition.setDelay(Duration.millis(i * 300));
            scaleTransition.play();

            dotsBox.getChildren().add(circle);
            dotsList.add(circle);

            //Append a right 'padding' if i < 2
            if(i < 2) {
                Pane pane = new Pane();
                pane.setPrefWidth(30f);
                pane.setPrefHeight(circle.getRadius() * 2 + 10);

                dotsBox.getChildren().add(pane);
            }

            //Configure the right circle's size
            circle.setRadius(circle.getRadius() / 1.75);
        }
    }

    public void setParent(ScanWaveView parent) {
        if(this.parent == null)
            this.parent = parent;
    }

    private class ClockAnimation {
        private static Transition current;

        //Animação para carregar o usuário
        private Transition animateClock(
                EventHandler<ActionEvent> onRotate
        ){
            SequentialTransition transition = new SequentialTransition(
                    getDisplayUserAnimation(onRotate),
                    getDismissAnimation()
            );

            transition.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    finishDismissAnimation();
                    fadeContainer();
                }
            });

            return new SequentialTransition(transition);
        }

        //Animação para quem já bateu o ponto, dá um aviso
        public SequentialTransition alreadyClocked(Node text1, Node text2){
            ParallelTransition parallel = new ParallelTransition();
            parallel.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {

                }
            });
            for(Circle c : dotsList){
                FillTransition errorFill = new FillTransition(Duration.millis(500), c, Color.WHITE, Color.TRANSPARENT);
                parallel.getChildren().add(errorFill);
            }

            PauseTransition pauseTransition = new PauseTransition(Duration.seconds(5));
            parallel.getChildren().add(pauseTransition);

            parallel.setAutoReverse(true);
            parallel.setCycleCount(2);

            text1.setVisible(true);
            text2.setVisible(true);

            return new SequentialTransition(parallel);
        }

        //Animação para tá tentando entrar no trabalho antes do tempo minimo permitido pela legislação
        public SequentialTransition cantWork(Label text1, Label text2, Label text3, Node container){
            ParallelTransition parallel = new ParallelTransition();

            for(Circle c : dotsList){
                FillTransition errorFill = new FillTransition(Duration.millis(500), c, Color.WHITE, Color.TRANSPARENT);
                parallel.getChildren().add(errorFill);
            }

            PauseTransition pauseTransition = new PauseTransition(Duration.seconds(5));
            parallel.getChildren().add(pauseTransition);

            parallel.setAutoReverse(true);
            parallel.setCycleCount(2);

            text1.setVisible(true);
            text2.setVisible(true);
            text3.setVisible(true);
            container.setVisible(true);

            return new SequentialTransition(parallel);
        }

        //Animação para quem forçou a saída
        public SequentialTransition forceClockOut(){
            if(current != null)
                current.stop();

            ParallelTransition parallelTransition = new ParallelTransition();
            PauseTransition pauseTransition = new PauseTransition(Duration.seconds(3));
            parallelTransition.getChildren().add(pauseTransition);


            ParallelTransition parallel = new ParallelTransition();
            for(Circle c : dotsList){
                FillTransition errorFill = new FillTransition(Duration.millis(500), c, Color.TRANSPARENT, Color.WHITE);
                parallel.getChildren().add(errorFill);
            }

            return new SequentialTransition(parallelTransition, parallel);
        }

        //Animação para usuário inexistente
        public SequentialTransition wrongUser(){
            ParallelTransition parallelRed = new ParallelTransition();

            for(Circle c : dotsList){
                FillTransition errorFill = new FillTransition(Duration.millis(500), c, Color.WHITE, Color.RED);
                errorFill.setCycleCount(2);
                errorFill.setAutoReverse(true);
                parallelRed.getChildren().add(errorFill);
            }

            return new SequentialTransition(parallelRed);
        }


        private SequentialTransition getDisplayUserAnimation(EventHandler<ActionEvent> onRotate){
            SequentialTransition sequentialTransition = new SequentialTransition();

            ParallelTransition parallel = new ParallelTransition();

            //Configure parallel rotation
            RotateTransition rotation = getImageRotation();
            rotation.setOnFinished(onRotate);
            parallel.getChildren().add(rotation);

            //Configure parallel circle display
            for(Circle c : dotsList){
                FillTransition transparentCircleFill = new FillTransition(Duration.millis(600), c, Color.WHITE, Color.TRANSPARENT);
                parallel.getChildren().add(transparentCircleFill);
            }

            sequentialTransition.getChildren().add(parallel);

            //Configure parallel imageScale
            ScaleTransition imageScale = new ScaleTransition(Duration.millis(300));
            imageScale.setNode(userImage);
            imageScale.setToY(1.15);
            imageScale.setToX(1.15);
            imageScale.setCycleCount(2);
            imageScale.setAutoReverse(true);
            sequentialTransition.getChildren().add(imageScale);

            //Configure a interval to dismiss user information
            PauseTransition interval = new PauseTransition(Duration.seconds(1.5));
            sequentialTransition.getChildren().add(interval);

            return sequentialTransition;
        }

        private TranslateTransition getDismissAnimation(){
            TranslateTransition translate = new TranslateTransition(Duration.millis(500), interfaceContainer);
            translate.setToX(0 - interfaceContainer.getWidth());
            return translate;
        }

        private void finishDismissAnimation(){
            interfaceContainer.setTranslateX(0);

            for (Circle c : dotsList) {
                c.setFill(Color.WHITE);
            }

            displayUser(false);
        }
    }
}
