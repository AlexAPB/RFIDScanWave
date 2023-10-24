package com.fatec.rfidscanwave.db;

import com.fatec.rfidscanwave.model.Clock;
import com.fatec.rfidscanwave.model.ClockDay;
import com.fatec.rfidscanwave.model.EmployeeModel;
import javafx.scene.image.Image;

public interface IScanWaveDB {
    int getIdByRFID(String rfid);
    int getJobIdById(int id);
    EmployeeModel getEmployee(int id);
    String getJobById(int id);
    String getName(int id);
    int getWorkdayDuration(int id);
    Image getUserImageById(int id);
    void clock(int id);
    Clock getLastClock(int id);
    ClockDay getLastClockDay(int id);

    boolean canClock(int id);
    int getLastClockDifferenceInSeconds(int id);
}
