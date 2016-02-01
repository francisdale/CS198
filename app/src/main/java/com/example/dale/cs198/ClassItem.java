package com.example.dale.cs198;

/**
 * Created by DALE on 1/21/2016.
 */
public class ClassItem {

    public ClassItem(String name, String startTime, String endTime){
        this.setName(name);
        this.setStartTime(startTime);
        this.setEndTime(endTime);
    }

    private String name;
    private String startTime;
    private String endTime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
