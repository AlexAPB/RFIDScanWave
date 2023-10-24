package com.fatec.rfidscanwave.model;

import javafx.scene.image.Image;

public class EmployeeModel {
    private int id;
    private String name;
    private String career;
    private Image image;

    public EmployeeModel(){

    }

    public EmployeeModel(int id, String name, String career, Image image){
        this.id = id;
        this.name = name;
        this.career = career;
        this.image = image;
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
