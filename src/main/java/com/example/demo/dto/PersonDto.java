package com.example.demo.dto;

import com.example.demo.entity.ActiveStatus;
import com.example.demo.entity.EmployeeRole;
import com.example.demo.entity.Person;
import com.example.demo.entity.PersonType;

import java.time.LocalDate;

public class PersonDto {

    private Long id;
    private PersonType type;
    private String fullName;
    private String phone;
    private String employeeCode;
    private EmployeeRole role;
    private LocalDate hireDate;
    private ActiveStatus status;

    public static PersonDto from(Person p) {
        PersonDto dto = new PersonDto();
        dto.setId(p.getId());
        dto.setType(p.getType());
        dto.setFullName(p.getFullName());
        dto.setPhone(p.getPhone());
        dto.setEmployeeCode(p.getEmployeeCode());
        dto.setRole(p.getRole());
        dto.setHireDate(p.getHireDate());
        dto.setStatus(p.getStatus());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PersonType getType() {
        return type;
    }

    public void setType(PersonType type) {
        this.type = type;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public EmployeeRole getRole() {
        return role;
    }

    public void setRole(EmployeeRole role) {
        this.role = role;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public ActiveStatus getStatus() {
        return status;
    }

    public void setStatus(ActiveStatus status) {
        this.status = status;
    }
}