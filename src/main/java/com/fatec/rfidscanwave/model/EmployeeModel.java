package com.fatec.rfidscanwave.model;

import com.fatec.rfidscanwave.model.clock.ClockModel;
import com.fatec.rfidscanwave.model.clock.ClockDayModel;
import javafx.scene.image.Image;

import java.util.List;

public class EmployeeModel {
    private int id;
    private String name;
    private String career;
    private Image image;
    private List<ClockDayModel> clockList;
    private ShiftModel shift;

    public EmployeeModel(){

    }

    public EmployeeModel(int id, String name, String career, Image image){
        this.id = id;
        this.name = name;
        this.career = career;
        this.image = image;
    }

    public List<ClockDayModel> getClockList() {
        return clockList;
    }

    public void setClockList(List<ClockDayModel> clockList) {
        this.clockList = clockList;
    }

    public ShiftModel getShift() {
        return shift;
    }

    public void setShift(ShiftModel shift) {
        this.shift = shift;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCareer(String career) {
        this.career = career;
    }

    public String getCareer() {
        return career;
    }

    public Image getImage() {
        return image;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
