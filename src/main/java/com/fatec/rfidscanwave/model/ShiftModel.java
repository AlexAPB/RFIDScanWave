package com.fatec.rfidscanwave.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class ShiftModel {
    private int id;
    private LocalTime clockInTime;
    private LocalTime clockOutTime;
    private LocalTime breakDuration;

    public ShiftModel(){

    }

    public ShiftModel(int id, LocalTime clockInTime, LocalTime clockOutTime, LocalTime breakDuration){
        this.id = id;
        this.clockInTime = clockInTime;
        this.clockOutTime = clockOutTime;
        this.breakDuration = breakDuration;
    }


    public Duration getWorkdayDuration(){
        return Duration.between(clockInTime, clockOutTime);
    }

    public LocalTime getBreakOutTime(){
        return LocalTime.from(clockInTime)
                .plusSeconds(clockOutTime.toSecondOfDay() / 2)
                .minusSeconds(breakDuration.toSecondOfDay() / 2);
    }

    public LocalTime getBreakReturnTime(){
        return LocalTime.from(clockInTime)
                .plusSeconds(clockOutTime.toSecondOfDay() / 2)
                .plusSeconds(breakDuration.toSecondOfDay() / 2);
    }

    public int getId() {
        return id;
    }

    public LocalTime getClockInTime() {
        return clockInTime;
    }

    public LocalTime getClockOutTime() {
        return clockOutTime;
    }

    public LocalTime getBreakDuration() {
        return breakDuration;
    }
}
