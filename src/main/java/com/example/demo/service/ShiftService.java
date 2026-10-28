package com.example.demo.service;

import com.example.demo.dto.ShiftDto;
import com.example.demo.dto.ShiftRequest;
import com.example.demo.entity.Shift;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.ShiftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ShiftService {

    private final ShiftRepository shiftRepository;

    public ShiftService(ShiftRepository shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    @Transactional(readOnly = true)
    public List<ShiftDto> findAll() {
        return shiftRepository.findAll().stream().map(ShiftDto::from).toList();
    }

    @Transactional(readOnly = true)
    public ShiftDto findById(Long id) {
        return ShiftDto.from(getEntity(id));
    }

    public ShiftDto create(ShiftRequest request) {
        validate(request);
        Shift shift = new Shift();
        applyFields(shift, request);
        return ShiftDto.from(shiftRepository.save(shift));
    }

    public ShiftDto update(Long id, ShiftRequest request) {
        Shift shift = getEntity(id);
        validate(request);
        applyFields(shift, request);
        return ShiftDto.from(shiftRepository.save(shift));
    }

    public void delete(Long id) {
        shiftRepository.delete(getEntity(id));
    }

    private void validate(ShiftRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw ApiException.badRequest("Tên ca không được để trống");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw ApiException.badRequest("Giờ bắt đầu/kết thúc ca không được để trống");
        }
    }

    private void applyFields(Shift shift, ShiftRequest request) {
        shift.setName(request.getName());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());
        shift.setRequiredStaff(request.getRequiredStaff() != null ? request.getRequiredStaff() : 1);
        shift.setActive(request.getActive() != null ? request.getActive() : true);
    }

    private Shift getEntity(Long id) {
        return shiftRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy ca làm việc id=" + id));
    }
}