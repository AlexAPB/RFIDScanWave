package com.fatec.rfidscanwave.model;

import java.time.LocalDateTime;
import java.util.Random;

public class Clock {
    private LocalDateTime clock;
    private ClockState state;
    public Clock(){

    }

    public Clock(LocalDateTime clock){
        this.clock = clock;
    }

    public Clock(LocalDateTime clock, ClockState state){
        this.clock = clock;
        this.state = state;
    }

    public String clockToHourMinute(){
        StringBuffer str = new StringBuffer();

        if(clock.getHour() < 10)
            str.append(0);

        str.append(clock.getHour()).append(":");

        if(clock.getMinute() < 10)
            str.append(0);

        str.append(clock.getMinute());

        return str.toString();
    }

    public void setClock(LocalDateTime clock) {
        this.clock = clock;
    }

    public void setState(ClockState state) {
        this.state = state;
    }

    public LocalDateTime getClock() {
        return clock;
    }

    public ClockState getState() {
        return state;
    }

    public enum ClockState {
        UNDEFINED (0),
        CLOCK_IN (1),
        CLOCK_OUT (2);

        private final int state;

        ClockState(int state){
            this.state = state;
        }

        public int getState() {
            return state;
        }

        public static ClockState nextState(ClockState state){
            return switch (state){
                case CLOCK_IN -> CLOCK_OUT;
                case CLOCK_OUT, UNDEFINED -> CLOCK_IN;
            };
        }

        public static ClockState fromState(int state){
            switch (state){
                case 0:
                    return ClockState.UNDEFINED;
                case 1:
                    return ClockState.CLOCK_IN;
                case 2:
                    return ClockState.CLOCK_OUT;
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