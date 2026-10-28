package com.example.demo.service;

import com.example.demo.dto.PersonDto;
import com.example.demo.dto.PersonRequest;
import com.example.demo.entity.ActiveStatus;
import com.example.demo.entity.EmployeeRole;
import com.example.demo.entity.Person;
import com.example.demo.entity.PersonType;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.PersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PersonService {

    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Transactional(readOnly = true)
    public List<PersonDto> findAll() {
        return personRepository.findAll().stream().map(PersonDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<PersonDto> findByType(PersonType type) {
        return personRepository.findByType(type).stream().map(PersonDto::from).toList();
    }

    @Transactional(readOnly = true)
    public PersonDto findById(Long id) {
        return PersonDto.from(getEntity(id));
    }

    public PersonDto create(PersonRequest request) {
        validate(request);
        if (request.getPhone() != null && personRepository.findByPhone(request.getPhone()).isPresent()) {
            throw ApiException.badRequest("Số điện thoại đã tồn tại");
        }
        if (request.getEmployeeCode() != null && personRepository.findByEmployeeCode(request.getEmployeeCode()).isPresent()) {
            throw ApiException.badRequest("Mã nhân viên đã tồn tại");
        }
        Person person = new Person();
        applyFields(person, request);
        return PersonDto.from(personRepository.save(person));
    }

    public PersonDto update(Long id, PersonRequest request) {
        Person person = getEntity(id);
        validate(request);
        String existingPassword = person.getPassword();
        applyFields(person, request);
        if (request.getType() == PersonType.EMPLOYEE
                && (request.getPassword() == null || request.getPassword().isBlank())) {
            person.setPassword(existingPassword);
        }
        return PersonDto.from(personRepository.save(person));
    }

    public void delete(Long id) {
        personRepository.delete(getEntity(id));
    }

    private void validate(PersonRequest request) {
        if (request.getType() == null) {
            throw ApiException.badRequest("Loại người dùng (type) không được để trống");
        }
        if (request.getType() == PersonType.EMPLOYEE && (request.getEmployeeCode() == null || request.getEmployeeCode().isBlank())) {
            throw ApiException.badRequest("Nhân viên cần có mã nhân viên");
        }
    }

    private void applyFields(Person person, PersonRequest request) {
        person.setType(request.getType());
        person.setFullName(request.getFullName());
        person.setPhone(request.getPhone());

        if (request.getType() == PersonType.EMPLOYEE) {
            person.setEmployeeCode(request.getEmployeeCode());
            person.setRole(request.getRole() != null ? request.getRole() : EmployeeRole.CASHIER);
            person.setHireDate(request.getHireDate());
            person.setPassword(request.getPassword() != null && !request.getPassword().isBlank()
                    ? request.getPassword() : "123456");
        } else {
            person.setEmployeeCode(null);
            person.setRole(null);
            person.setHireDate(null);
            person.setPassword(null);
        }
        person.setStatus(request.getStatus() != null ? request.getStatus() : ActiveStatus.ACTIVE);
    }

    private Person getEntity(Long id) {
        return personRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy người dùng id=" + id));
    }
}
