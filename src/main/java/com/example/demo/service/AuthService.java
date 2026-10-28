package com.example.demo.service;

import com.example.demo.dto.AuthUserDto;
import com.example.demo.dto.LoginRequest;
import com.example.demo.entity.ActiveStatus;
import com.example.demo.entity.Person;
import com.example.demo.entity.PersonType;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.PersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final PersonRepository personRepository;

    public AuthService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Transactional(readOnly = true)
    public AuthUserDto login(LoginRequest request) {
        if (request.getEmployeeCode() == null || request.getEmployeeCode().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            throw ApiException.badRequest("Mã nhân viên và mật khẩu không được để trống");
        }

        Person person = personRepository.findByEmployeeCodeAndPassword(
                        request.getEmployeeCode().trim(), request.getPassword())
                .orElseThrow(() -> ApiException.badRequest("Sai mã nhân viên hoặc mật khẩu"));

        if (person.getType() != PersonType.EMPLOYEE) {
            throw ApiException.badRequest("Tài khoản không phải nhân viên");
        }
        if (person.getStatus() == ActiveStatus.INACTIVE) {
            throw ApiException.badRequest("Tài khoản đã nghỉ việc");
        }

        return AuthUserDto.from(person);
    }
}