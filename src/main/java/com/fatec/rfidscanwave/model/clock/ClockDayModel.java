package com.fatec.rfidscanwave.model.clock;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;

public class ClockDayModel {
    private ClockModel clockIn;
    private ClockModel lunchOut;
    private ClockModel lunchReturn;
    private ClockModel clockOut;
    private boolean offDuty = false;

    public ClockDayModel(){
    }

    public ClockDayModel(boolean offDuty){
        this.offDuty = offDuty;
    }

    public ClockModel.ClockState getLastState() {
        if (clockOut != null)
            return ClockModel.ClockState.CLOCK_OUT;
        else if (lunchReturn != null)
            return ClockModel.ClockState.LUNCH_RETURN;
        else if (lunchOut != null)
            return ClockModel.ClockState.LUNCH_OUT;
        else if (clockIn != null)
            return ClockModel.ClockState.CLOCK_IN;
        else if (offDuty)
            return ClockModel.ClockState.OFF_DUTY;

        return ClockModel.ClockState.UNDEFINED;
    }

    public ClockModel getLastClock() {
        if (clockOut != null)
            return clockOut;
        else if (lunchReturn != null)
            return lunchReturn;
        else if (lunchOut != null)
            return lunchOut;
        else if (clockIn != null)
            return clockIn;

        return null;
    }

    public void setClockOut(ClockModel clockOut) {
        this.clockOut = clockOut;
    }

    public void setClockIn(ClockModel clockIn) {
        this.clockIn = clockIn;
    }

    public String difference(){
        if(clockOut == null || clockIn == null)
            return "Erro";

        StringBuffer str = new StringBuffer();

        Duration duration = Duration.between(
                LocalDateTime.of(clockIn.getDate(), clockIn.getTime()),
                LocalDateTime.of(clockOut.getDate(), clockOut.getTime())
        );

        if(duration.toHours() > 0) {
            str.append(duration.toHours()).append(" hora");

            if(duration.toHours() > 1)
                str.append("s");
        } else if(duration.toMinutes() > 0){
            str.append(duration.toMinutes()).append(" minuto");

            if(duration.toMinutes() > 1)
                str.append("s");
        } else {
            str.append(duration.toSeconds()).append(" segundo");

            if(duration.toSeconds() > 1)
                str.append("s");

        }

        return str.toString();
    }

    public boolean canSetClock(int stateNumber){
        boolean canSet = true;
        ClockModel.ClockState state = ClockModel.ClockState.fromState(stateNumber);

        switch (state){
            case CLOCK_IN -> {
                canSet = clockIn == null;
            }
            case LUNCH_OUT -> {
                canSet = lunchOut == null && clockIn == null;
            }
            case LUNCH_RETURN -> {
                canSet = lunchReturn == null && lunchOut == null && clockIn == null;
            }
            case CLOCK_OUT -> {
                canSet = clockOut == null && lunchReturn == null && lunchOut == null && clockIn == null;
            }
        }

        return canSet;
    }

    public void setClock(Timestamp dateStamp, Timestamp timestamp, int state){
        switch (state){
            case 1:
                setClockIn(new ClockModel(dateStamp.toLocalDateTime().toLocalDate(), timestamp.toLocalDateTime().toLocalTime(), ClockModel.ClockState.fromState(state)));
                break;
            case 2:
                setLunchOut(new ClockModel(dateStamp.toLocalDateTime().toLocalDate(), timestamp == null ? null :timestamp.toLocalDateTime().toLocalTime(), ClockModel.ClockState.fromState(state)));
                break;
            case 3:
                setLunchReturn(new ClockModel(dateStamp.toLocalDateTime().toLocalDate(),timestamp == null ? null : timestamp.toLocalDateTime().toLocalTime(), ClockModel.ClockState.fromState(state)));
                break;
            case 4:
                setClockOut(new ClockModel(dateStamp.toLocalDateTime().toLocalDate(), timestamp == null ? null : timestamp.toLocalDateTime().toLocalTime(), ClockModel.ClockState.fromState(state)));
                break;
        }
    }

    public void setLunchOut(ClockModel lunchOut) {
        this.lunchOut = lunchOut;
    }

    public void setLunchReturn(ClockModel lunchReturn) {
        this.lunchReturn = lunchReturn;
    }

    public ClockModel getLunchOut() {
        return lunchOut;
    }

    public ClockModel getLunchReturn() {
        return lunchReturn;
    }

    public ClockModel getClockIn() {
        return clockIn;
    }

    public ClockModel getClockOut() {
        return clockOut;
    }

    public boolean isOffDuty() {
        return offDuty;
    }
}
