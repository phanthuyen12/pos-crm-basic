package com.example.demo.dto;

import com.example.demo.entity.Shift;

import java.time.LocalTime;

public class ShiftDto {

    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer requiredStaff;
    private Boolean active;

    public static ShiftDto from(Shift s) {
        ShiftDto dto = new ShiftDto();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setStartTime(s.getStartTime());
        dto.setEndTime(s.getEndTime());
        dto.setRequiredStaff(s.getRequiredStaff());
        dto.setActive(s.getActive());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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