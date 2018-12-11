package com.example.roche.reactivescreen;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Exercise implements Serializable {

    private int id;
    private String date;
    private int steps;

    public Exercise() {
        int id = -1;
        this.date = "";
        this.steps = 0;
    }

    static String dateToStr() {
        Date d = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        return formatter.format(d);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    @Override
    public String toString() {
        return this.date + " " + this.steps;
    }
}