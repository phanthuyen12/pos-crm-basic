package com.example.demo.dto;

import com.example.demo.entity.Person;
import com.example.demo.entity.Shift;
import com.example.demo.entity.ShiftAssignment;
import com.example.demo.entity.ShiftAssignmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ShiftAssignmentDto {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private Long shiftId;
    private String shiftName;
    private LocalDate workDate;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private ShiftAssignmentStatus status;

    public static ShiftAssignmentDto from(ShiftAssignment a) {
        ShiftAssignmentDto dto = new ShiftAssignmentDto();
        dto.setId(a.getId());
        Person e = a.getEmployee();
        Shift s = a.getShift();
        if (e != null) {
            dto.setEmployeeId(e.getId());
            dto.setEmployeeName(e.getFullName());
        }
        if (s != null) {
            dto.setShiftId(s.getId());
            dto.setShiftName(s.getName());
        }
        dto.setWorkDate(a.getWorkDate());
        dto.setCheckInTime(a.getCheckInTime());
        dto.setCheckOutTime(a.getCheckOutTime());
        dto.setStatus(a.getStatus());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public Long getShiftId() {
        return shiftId;
    }

    public void setShiftId(Long shiftId) {
        this.shiftId = shiftId;
    }

    public String getShiftName() {
        return shiftName;
    }

    public void setShiftName(String shiftName) {
        this.shiftName = shiftName;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public ShiftAssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(ShiftAssignmentStatus status) {
        this.status = status;
    }
}