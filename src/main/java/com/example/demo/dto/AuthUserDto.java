package com.example.demo.dto;

import com.example.demo.entity.EmployeeRole;
import com.example.demo.entity.Person;

public class AuthUserDto {

    private Long id;
    private String employeeCode;
    private String fullName;
    private EmployeeRole role;

    public static AuthUserDto from(Person p) {
        AuthUserDto dto = new AuthUserDto();
        dto.setId(p.getId());
        dto.setEmployeeCode(p.getEmployeeCode());
        dto.setFullName(p.getFullName());
        dto.setRole(p.getRole());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public EmployeeRole getRole() {
        return role;
    }

    public void setRole(EmployeeRole role) {
        this.role = role;
    }
}