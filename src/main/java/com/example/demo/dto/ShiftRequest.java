package com.example.demo.dto;

import java.time.LocalTime;

public class ShiftRequest {

    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer requiredStaff;
    private Boolean active;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Integer getRequiredStaff() {
        return requiredStaff;
    }

    public void setRequiredStaff(Integer requiredStaff) {
        this.requiredStaff = requiredStaff;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}