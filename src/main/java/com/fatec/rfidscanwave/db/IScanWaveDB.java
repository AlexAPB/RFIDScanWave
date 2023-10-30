package com.fatec.rfidscanwave.db;

import com.fatec.rfidscanwave.model.clock.ClockDayModel;
import com.fatec.rfidscanwave.model.clock.ClockModel;
import com.fatec.rfidscanwave.model.EmployeeModel;
import javafx.scene.image.Image;

import java.util.List;

public interface IScanWaveDB {
    int getIdByRFID(String rfid);
    EmployeeModel getEmployee(int id);
    List<ClockDayModel> getClockListById(int employeeId);
    Image getUserImageById(int id);
    void clock(int id, EmployeeModel employee, ClockModel clock);
}
