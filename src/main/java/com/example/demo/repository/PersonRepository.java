package com.example.demo.repository;

import com.example.demo.entity.Person;
import com.example.demo.entity.PersonType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {

    List<Person> findByType(PersonType type);

    Optional<Person> findByPhone(String phone);

    Optional<Person> findByEmployeeCode(String employeeCode);

    Optional<Person> findByEmployeeCodeAndPassword(String employeeCode, String password);
}