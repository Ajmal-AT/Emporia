package com.Emporia.Repository;

import com.Emporia.Entity.Employees;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeesRepository extends JpaRepository<Employees, Long> {

    Employees findByEmployeeId(String employeeId);

    boolean existsByEmployeeId(String employeeId);

    Employees findByPhoneNumber(String phoneNumber);


    Employees findByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);
}
