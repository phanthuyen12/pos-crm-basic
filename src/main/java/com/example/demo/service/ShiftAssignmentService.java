package com.example.demo.service;

import com.example.demo.dto.ShiftAssignmentDto;
import com.example.demo.dto.ShiftAssignmentRequest;
import com.example.demo.entity.Person;
import com.example.demo.entity.Shift;
import com.example.demo.entity.ShiftAssignment;
import com.example.demo.entity.ShiftAssignmentStatus;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.PersonRepository;
import com.example.demo.repository.ShiftAssignmentRepository;
import com.example.demo.repository.ShiftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ShiftAssignmentService {

    private final ShiftAssignmentRepository assignmentRepository;
    private final PersonRepository personRepository;
    private final ShiftRepository shiftRepository;

    public ShiftAssignmentService(ShiftAssignmentRepository assignmentRepository,
                                  PersonRepository personRepository,
                                  ShiftRepository shiftRepository) {
        this.assignmentRepository = assignmentRepository;
        this.personRepository = personRepository;
        this.shiftRepository = shiftRepository;
    }

    @Transactional(readOnly = true)
    public List<ShiftAssignmentDto> findAll() {
        return assignmentRepository.findAll().stream().map(ShiftAssignmentDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ShiftAssignmentDto> findByDate(LocalDate date) {
        return assignmentRepository.findByWorkDate(date).stream().map(ShiftAssignmentDto::from).toList();
    }

    @Transactional(readOnly = true)
    public ShiftAssignmentDto findById(Long id) {
        return ShiftAssignmentDto.from(getEntity(id));
    }

    public ShiftAssignmentDto create(ShiftAssignmentRequest request) {
        if (request.getEmployeeId() == null || request.getShiftId() == null || request.getWorkDate() == null) {
            throw ApiException.badRequest("employeeId, shiftId, workDate không được để trống");
        }
        Person employee = personRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy nhân viên id=" + request.getEmployeeId()));
        Shift shift = shiftRepository.findById(request.getShiftId())
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy ca id=" + request.getShiftId()));

        boolean exists = assignmentRepository.existsByEmployeeIdAndShiftIdAndWorkDate(
                request.getEmployeeId(), request.getShiftId(), request.getWorkDate());
        if (exists) {
            throw ApiException.badRequest("Nhân viên đã được xếp ca này trong ngày");
        }

        ShiftAssignment assignment = new ShiftAssignment();
        assignment.setEmployee(employee);
        assignment.setShift(shift);
        assignment.setWorkDate(request.getWorkDate());
        assignment.setStatus(request.getStatus() != null ? request.getStatus() : ShiftAssignmentStatus.SCHEDULED);
        return ShiftAssignmentDto.from(assignmentRepository.save(assignment));
    }

    public ShiftAssignmentDto checkIn(Long id) {
        ShiftAssignment assignment = getEntity(id);
        assignment.setCheckInTime(LocalDateTime.now());
        assignment.setStatus(ShiftAssignmentStatus.IN_PROGRESS);
        return ShiftAssignmentDto.from(assignmentRepository.save(assignment));
    }

    public ShiftAssignmentDto checkOut(Long id) {
        ShiftAssignment assignment = getEntity(id);
        assignment.setCheckOutTime(LocalDateTime.now());
        assignment.setStatus(ShiftAssignmentStatus.COMPLETED);
        return ShiftAssignmentDto.from(assignmentRepository.save(assignment));
    }

    public ShiftAssignmentDto update(Long id, ShiftAssignmentRequest request) {
        ShiftAssignment assignment = getEntity(id);
        if (request.getStatus() != null) {
            assignment.setStatus(request.getStatus());
        }
        return ShiftAssignmentDto.from(assignmentRepository.save(assignment));
    }

    public void delete(Long id) {
        assignmentRepository.delete(getEntity(id));
    }

    private ShiftAssignment getEntity(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy phân ca id=" + id));
    }
}