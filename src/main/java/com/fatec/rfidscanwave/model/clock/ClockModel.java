package com.fatec.rfidscanwave.model.clock;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;

public class ClockModel {
    private LocalDate date;
    private LocalTime time;
    private ClockState state;
    public ClockModel(){

    }

    public ClockModel(LocalDate date, LocalTime time){
        this.date = date;
        this.time = time;
    }

    public ClockModel(LocalDate date, LocalTime time, ClockState state){
        this.date = date;
        this.time = time;
        this.state = state;
    }

    public String clockToHourMinute(){
        StringBuffer str = new StringBuffer();

        if(time.getHour() < 10)
            str.append(0);

        str.append(time.getHour()).append(":");

        if(time.getMinute() < 10)
            str.append(0);

        str.append(time.getMinute());

        return str.toString();
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public void setState(ClockState state) {
        this.state = state;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public ClockState getState() {
        return state;
    }

    public enum ClockState {
        UNDEFINED (0),
        CLOCK_IN (1),
        LUNCH_OUT (2),
        LUNCH_RETURN (3),
        CLOCK_OUT (4),
        OFF_DUTY (5);

        private final int state;

        ClockState(int state){
            this.state = state;
        }

        public int getState() {
            return state;
        }

        public static ClockState nextState(ClockState state){
            return switch (state){
                case CLOCK_IN -> LUNCH_OUT;
                case LUNCH_OUT -> LUNCH_RETURN;
                case LUNCH_RETURN -> CLOCK_OUT;
                case CLOCK_OUT, UNDEFINED -> CLOCK_IN;
                default -> CLOCK_IN;
            };
        }

        public ClockState getNext(){
            return switch (this){
                case CLOCK_IN -> LUNCH_OUT;
                case LUNCH_OUT -> LUNCH_RETURN;
                case LUNCH_RETURN -> CLOCK_OUT;
                case OFF_DUTY, CLOCK_OUT, UNDEFINED -> CLOCK_IN;
            };
        }

        public static ClockState fromState(int state){
            switch (state){
                case 0:
                    return UNDEFINED;
                case 1:
                    return CLOCK_IN;
                case 2:
                    return LUNCH_OUT;
                case 3:
                    return LUNCH_RETURN;
                case 4:
                    return CLOCK_OUT;
                case 5:
                    return OFF_DUTY;
                default:
                    return null;
            }
        }

        public static String getGreetings(ClockState state){
            String greetings[] = null;
            Random rnd = new Random();

            switch (state){
                case CLOCK_IN -> {
                    greetings = new String[]{
                      "Tenha um dia produtivo!",
                      "Seja bem-vindo!",
                      "Seu dia de trabalho começa agora.",
                      "Início do expediente.",
                      "Tenha um dia produtivo!"
                    };
                }
                case CLOCK_OUT -> {
                    greetings = new String[]{
                            "Bom trabalho!",
                            "Até logo!",
                            "Missão cumprida!",
                            "Tenha um ótimo descanso!",
                            "Obrigado pelo esforço!",
                            "Fim do expediente"
                    };
                }
            };

            if(greetings == null)
                return "Bom trabalho!";

            return greetings[rnd.nextInt(greetings.length)];
        }
    }
}