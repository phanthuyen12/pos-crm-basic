package com.example.demo.repository;

import com.example.demo.entity.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {

    List<ShiftAssignment> findByWorkDate(LocalDate workDate);

    List<ShiftAssignment> findByEmployeeId(Long employeeId);

    boolean existsByEmployeeIdAndShiftIdAndWorkDate(Long employeeId, Long shiftId, LocalDate workDate);
}