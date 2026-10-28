package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PersonDto;
import com.example.demo.dto.PersonRequest;
import com.example.demo.entity.PersonType;
import com.example.demo.service.PersonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/people")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping
    public ApiResponse<List<PersonDto>> list(@RequestParam(required = false) PersonType type) {
        if (type != null) {
            return ApiResponse.ok(personService.findByType(type));
        }
        return ApiResponse.ok(personService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<PersonDto> get(@PathVariable Long id) {
        return ApiResponse.ok(personService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PersonDto>> create(@RequestBody PersonRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.ok("Tạo người dùng thành công", personService.create(request)));
    }

    @PutMapping("/{id}")
    public ApiResponse<PersonDto> update(@PathVariable Long id, @RequestBody PersonRequest request) {
        return ApiResponse.ok("Cập nhật người dùng thành công", personService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        personService.delete(id);
        return ApiResponse.ok("Xóa người dùng thành công", null);
    }
}