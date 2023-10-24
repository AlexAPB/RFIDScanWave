package com.fatec.rfidscanwave.util;

public class TimeUtil {
    public static String getTimeFromSeconds(int timeSeconds){
        StringBuffer time = new StringBuffer();

        int hours = Math.floorDiv(timeSeconds, 3600);
        int minutes = Math.floorDiv((timeSeconds - hours * 3600), 60);
        int seconds = (timeSeconds - hours * 3600 - minutes * 60);

        if(hours > 0){
            if(hours == 1)
                time.append(hours).append(" hora");
            else
                time.append(hours).append(" horas");
        }

        if(minutes > 0){
            if(hours > 0)
                time.append(" e ");

            if(minutes == 1)
                time.append(minutes).append(" minuto");
            else
                time.append(minutes).append(" minutos");
        }

        if(seconds >= 0 && hours == 0 && minutes == 0){
            if(seconds == 1)
                time.append(seconds).append(" segundo");
            else
                time.append(seconds).append(" segundos");
        }


        return time.toString();
    }
}
