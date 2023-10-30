package com.fatec.rfidscanwave.db;

import com.fatec.rfidscanwave.ScanWave;
import com.fatec.rfidscanwave.model.ShiftModel;
import com.fatec.rfidscanwave.model.clock.ClockModel;
import com.fatec.rfidscanwave.model.clock.ClockDayModel;
import com.fatec.rfidscanwave.model.EmployeeModel;
import javafx.scene.image.Image;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.List;

public class ScanWaveDB implements IScanWaveDB {
    private Connection db;

    public ScanWaveDB(){
        db = DB.getConnection();
    }

    public Connection getDb() {
        return db;
    }

    public void reloadDb(){
        if(db == null)
            db = DB.getConnection();
    }

    @Override
    public void clock(int id, EmployeeModel employee, ClockModel clock){
        PreparedStatement insertClock = null;

        if(clock.getState() == ClockModel.ClockState.UNDEFINED)
            return;

        try {
            String sqlDate = clock.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String sqlTime = clock.getTime() == null ? null : clock.getTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

            insertClock = db.prepareStatement(
                    "INSERT INTO clock(employee_id, employee_shift, clock_date, clock_time, clock_state)" + "\n" +
                            "VALUES ( ?, ?, ?, ?, ?), " +
                            "( ?, ?, ?, ?, ?), " +
                            "( ?, ?, ?, ?, ?), " +
                            "( ?, ?, ?, ?, ?); "
            );
            insertClock.setInt(1, id);
            insertClock.setInt(6, id);
            insertClock.setInt(11, id);
            insertClock.setInt(16, id);

            insertClock.setInt(2, employee.getShift().getId());
            insertClock.setInt(7, employee.getShift().getId());
            insertClock.setInt(12, employee.getShift().getId());
            insertClock.setInt(17, employee.getShift().getId());

            insertClock.setString(3, sqlDate);
            insertClock.setString(8, sqlDate);
            insertClock.setString(13, sqlDate);
            insertClock.setString(18, sqlDate);

            insertClock.setString(4, sqlTime);
            insertClock.setString(9, null);
            insertClock.setString(14, null);
            insertClock.setString(19, null);

            insertClock.setInt(5, 1);
            insertClock.setInt(10, 2);
            insertClock.setInt(15, 3);
            insertClock.setInt(20, 4);

            insertClock.execute();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void updateClock(int id, ClockModel clock){
        PreparedStatement insertClock = null;

        if(clock.getState() == ClockModel.ClockState.UNDEFINED)
            return;

        try {
            String sqlDate = clock.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String sqlTime = clock.getTime() == null ? null : clock.getTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

            insertClock = db.prepareStatement(
                    "UPDATE clock SET clock_time = ? WHERE employee_id = ? AND clock_date = ? AND clock_state = ?;"
            );
            insertClock.setString(1,sqlTime);
            insertClock.setInt(2, id);
            insertClock.setString(3, sqlDate);
            insertClock.setInt(4, clock.getState().getState());

            insertClock.execute();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public EmployeeModel getEmployee(int id){
        Statement statement = null;
        ResultSet resultSet = null;
        EmployeeModel employee = new EmployeeModel();

        try {
            statement = db.createStatement();
            resultSet = statement.executeQuery(
                    "SELECT name, job_name, shifts.* FROM employees" + "\n" +
                            "INNER JOIN jobs ON employees.job_id = jobs.id" + "\n" +
                            "INNER JOIN shifts ON shifts.id = employees.shift_id" + "\n" +
                            "WHERE employees.id = " + id + "\n"
            );

            if(!resultSet.next()) {
                employee.setClockList(new ArrayList<>());
                employee.getClockList().add(new ClockDayModel());
                statement.close();
                resultSet.close();
                return employee;
            }

            employee.setId(id);
            employee.setName(resultSet.getString("name"));
            employee.setCareer(resultSet.getString("job_name"));
            employee.setShift(
                    new ShiftModel(
                            resultSet.getInt("id"),
                            LocalTime.parse(resultSet.getTime("clock_in").toString()),
                            LocalTime.parse(resultSet.getTime("clock_out").toString()),
                            LocalTime.parse(resultSet.getTime("break_duration").toString())
                    )
            );
            employee.setClockList(getClockListById(employee.getId()));

            statement.close();
            resultSet.close();

            return employee;
        } catch(RuntimeException | SQLException e){
            e.printStackTrace();
        }

        return employee;
    }

    @Override
    public List<ClockDayModel> getClockListById(int employeeId){
        ResultSet resultSet = null;
        Statement statement = null;
        List<ClockDayModel> clockList = new ArrayList<>();

        try {
            statement = db.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            resultSet = statement.executeQuery(
                    "SELECT * FROM clock WHERE employee_id=" + employeeId + " ORDER BY clock_date DESC, clock_state DESC;"
            );

            if (!resultSet.next()) {
                clockList.add(new ClockDayModel());
                return clockList;
            }

            ClockDayModel clockDay = new ClockDayModel();

            do {
                int state = resultSet.getInt("clock_state");

                if(clockDay.canSetClock(state)){
                    if(resultSet.getTime("clock_time") != null)
                        clockDay.setClock(resultSet.getTimestamp("clock_date"), resultSet.getTimestamp("clock_time"), state);
                } else {
                    resultSet.previous();
                    clockList.add(clockDay);
                    clockDay = new ClockDayModel();
                }
            } while(resultSet.next());

            clockList.add(clockDay);

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return clockList;
    }

    @Override
    public int getIdByRFID(String rfid) {
        Statement statement = null;
        ResultSet resultSet = null;
        int id = -1;

        try {
            statement = db.createStatement();
            resultSet = statement.executeQuery("SELECT id FROM employees WHERE rfid=" + rfid);

            if(!resultSet.next()) {
                statement.close();
                resultSet.close();
                return id;
            }

            id = resultSet.getInt("id");

            statement.close();
            resultSet.close();

            return id;
        } catch(RuntimeException | SQLException e){
            e.printStackTrace();
        }

        return id;
    }

    @Override
    public Image getUserImageById(int id) {
        return new Image(ScanWave.class.getResource("/db/employees/" + id + ".jpg").toString());
    }
}
