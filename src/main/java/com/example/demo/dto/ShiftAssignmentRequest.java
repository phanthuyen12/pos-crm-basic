package com.example.demo.dto;

import com.example.demo.entity.ShiftAssignmentStatus;

import java.time.LocalDate;

public class ShiftAssignmentRequest {

    private Long employeeId;
    private Long shiftId;
    private LocalDate workDate;
    private ShiftAssignmentStatus status;

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Long getShiftId() {
        return shiftId;
    }

    public void setShiftId(Long shiftId) {
        this.shiftId = shiftId;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    public ShiftAssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(ShiftAssignmentStatus status) {
        this.status = status;
    }
}