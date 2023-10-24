package com.fatec.rfidscanwave.db;

import com.fatec.rfidscanwave.ScanWave;
import com.fatec.rfidscanwave.model.Clock;
import com.fatec.rfidscanwave.model.ClockDay;
import com.fatec.rfidscanwave.model.EmployeeModel;
import javafx.scene.image.Image;

import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

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

    public void clock(int id){
        Clock lastClock = getLastClock(id);
        Clock.ClockState nextState;
        LocalDateTime time = LocalDateTime.now();
        PreparedStatement preparedStatement = null;


        if (lastClock != null) {
            nextState = Clock.ClockState.nextState(lastClock.getState());
        } else {
            nextState = Clock.ClockState.CLOCK_IN;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String sqlDateTime = time.format(formatter);

            preparedStatement = db.prepareStatement(
                    "INSERT INTO clock(id, clock_time, clock_state)" + "\n" +
                            "VALUES ( ?, ?, ?);"
            );
            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, sqlDateTime);
            preparedStatement.setInt(3, nextState.getState());
            preparedStatement.executeUpdate();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public Clock getLastClock(int id){
        Clock clock = null;
        ResultSet resultSet = null;
        Statement statement = null;

        try {
            statement = db.createStatement();
            resultSet = statement.executeQuery(
                    "SELECT clock_time, clock_state FROM clock WHERE id=" + id + "\n" +
                            "ORDER BY clock_time DESC LIMIT 1"
            );

            if(!resultSet.next()){
                statement.close();
                resultSet.close();

                return clock;
            }

            clock = new Clock();

            clock.setClock(resultSet.getTimestamp("clock_time").toLocalDateTime());

            int state = resultSet.getInt("clock_state");
            clock.setState(Clock.ClockState.fromState(state));
        } catch(Exception e){
            e.printStackTrace();
            return clock;
        }

        return clock;
    }

    public ClockDay getLastClockDay(int id){
        ClockDay clock = null;
        ResultSet resultSet = null;
        Statement statement = null;

        try {
            statement = db.createStatement();
            resultSet = statement.executeQuery(
                    "SELECT clock_time, clock_state FROM clock WHERE id=" + id + "\n" +
                            "ORDER BY clock_time DESC LIMIT 2"
            );

            if(!resultSet.next()){
                statement.close();
                resultSet.close();

                return clock;
            }

            clock = new ClockDay();

            if(resultSet.getInt("clock_state") == Clock.ClockState.CLOCK_OUT.getState()){
                Clock clockOut = new Clock(
                        resultSet.getTimestamp("clock_time").toLocalDateTime(),
                        Clock.ClockState.CLOCK_OUT
                );

                resultSet.next();

                Clock clockIn = new Clock(
                        resultSet.getTimestamp("clock_time").toLocalDateTime(),
                        Clock.ClockState.CLOCK_IN
                );

                clock.setClockIn(clockIn);
                clock.setClockOut(clockOut);
            } else if(resultSet.getInt("clock_state") == Clock.ClockState.CLOCK_IN.getState()){
                Clock clockIn = new Clock(
                        resultSet.getTimestamp("clock_time").toLocalDateTime(),
                        Clock.ClockState.CLOCK_IN
                );

                clock.setClockIn(clockIn);
            }
        } catch(Exception e){
            e.printStackTrace();
            return clock;
        }

        return clock;
    }


    @Override
    public boolean canClock(int id) {
        Clock lastClock = getLastClock(id);

        if (lastClock != null) {
            Duration duration = Duration.between(lastClock.getClock(), LocalDateTime.now());

            if(lastClock.getState() == Clock.ClockState.CLOCK_OUT) {
                int workdayDuration = getWorkdayDuration(id);

                if(workdayDuration <= 6)
                    return true;
                else
                    return duration.toHours() > getMinToWork(workdayDuration);
            } else {
                return duration.toHours() > (getWorkdayDuration(id) * 0.8f);
            }
        }

        return true;
    }

    public int getMinToWork(int workdayDuration){
        if(workdayDuration < 12)
            return 11;
        else
            return 36;
    }

    @Override
    public int getLastClockDifferenceInSeconds(int id) {
        Clock lastClock = getLastClock(id);

        if (lastClock != null) {
            Duration duration = Duration.between(lastClock.getClock(), LocalDateTime.now());
            return (int) duration.toSeconds();
        }

        return 0;
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
    public int getWorkdayDuration(int id) {
        Statement statement = null;
        ResultSet resultSet = null;
        int duration = 0;

        try {
            statement = db.createStatement();
            resultSet = statement.executeQuery("SELECT workday_duration FROM employees WHERE id=" + id);

            if(!resultSet.next()) {
                statement.close();
                resultSet.close();
                return duration;
            }

            duration = resultSet.getInt("workday_duration");

            statement.close();
            resultSet.close();

            return duration;
        } catch(RuntimeException | SQLException e){
            e.printStackTrace();
        }

        return duration;
    }

    @Override
    public int getJobIdById(int id){
        Statement statement = null;
        ResultSet resultSet = null;
        int job_id = 0;

        try {
            statement = db.createStatement();
            resultSet = statement.executeQuery("SELECT job_id FROM employees WHERE id=" + id);

            if(!resultSet.next()) {
                statement.close();
                resultSet.close();
                return job_id;
            }

            job_id = resultSet.getInt("job_id");

            statement.close();
            resultSet.close();

            return job_id;
        } catch(RuntimeException | SQLException e){
            e.printStackTrace();
        }

        return job_id;
    }

    public EmployeeModel getEmployee(int id){
        Statement statement = null;
        ResultSet resultSet = null;
        EmployeeModel employee = new EmployeeModel();

        try {
            statement = db.createStatement();
            resultSet = statement.executeQuery(
                    "SELECT name, job_name FROM employees" + "\n" +
                            "INNER JOIN jobs ON employees.job_id = jobs.id" + "\n" +
                            "WHERE employees.id = " + id
            );

            if(!resultSet.next()) {
                statement.close();
                resultSet.close();
                return employee;
            }

            employee.setId(id);
            employee.setName(resultSet.getString("name"));
            employee.setCareer(resultSet.getString("job_name"));

            statement.close();
            resultSet.close();

            return employee;
        } catch(RuntimeException | SQLException e){
            e.printStackTrace();
        }

        return employee;
    }

    @Override
    public String getJobById(int id) {
        Statement statement = null;
        ResultSet resultSet = null;
        String career = "Career";

        try {
            statement = db.createStatement();
            resultSet = statement.executeQuery("SELECT job_name FROM jobs WHERE id=" + getJobIdById(id));

            if(!resultSet.next()) {
                statement.close();
                resultSet.close();
                return career;
            }

            career = resultSet.getString("job_name");

            statement.close();
            resultSet.close();

            return career;
        } catch(RuntimeException | SQLException e){
            e.printStackTrace();
        }

        return career;
    }

    @Override
    public String getName(int id) {
        Statement statement = null;
        ResultSet resultSet = null;
        String user = "User";

        try {
            statement = db.createStatement();
            resultSet = statement.executeQuery("SELECT name FROM employees WHERE id=" + id);

            if(!resultSet.next()) {
                statement.close();
                resultSet.close();
                return user;
            }

            user = resultSet.getString("name");

            statement.close();
            resultSet.close();

            return user;
        } catch(RuntimeException | SQLException e){
            e.printStackTrace();
        }

        return user;
    }

    @Override
    public Image getUserImageById(int id) {
        return new Image(ScanWave.class.getResource("/db/employees/" + id + ".jpg").toString());
    }
}
