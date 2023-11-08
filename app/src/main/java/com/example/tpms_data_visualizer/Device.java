package com.example.tpms_data_visualizer;


import com.google.firebase.Timestamp;

import java.util.ArrayList;

public class Device {
    String title;
    String content; //TODO modify content when you have the checked sensors
    Timestamp timestamp;

    ArrayList<String> checkedSensorsArray;

    public Device() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public ArrayList<String> getCheckedSensorsArray() {
        return checkedSensorsArray;
    }

    public void setCheckedSensorsArray(ArrayList<String> checkedSensorsArray) {
        this.checkedSensorsArray = checkedSensorsArray;
    }
}
