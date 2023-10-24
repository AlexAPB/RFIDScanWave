package com.fatec.rfidscanwave.model;

import java.time.Duration;
import java.time.LocalDateTime;

public class ClockDay {
    private Clock clockIn;
    private Clock clockOut;


    public void setClockOut(Clock clockOut) {
        this.clockOut = clockOut;
    }

    public void setClockIn(Clock clockIn) {
        this.clockIn = clockIn;
    }

    public String difference(){
        if(clockOut == null || clockIn == null)
            return "Erro";

        StringBuffer str = new StringBuffer();

        Duration duration = Duration.between(clockIn.getClock(), clockOut.getClock());

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

    public Clock getClockIn() {
        return clockIn;
    }

    public Clock getClockOut() {
        return clockOut;
    }

}
