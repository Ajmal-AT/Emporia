package com.Emporia.Repository;

import com.Emporia.Entity.Departments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentsRepository extends JpaRepository<Departments, Long> {
    Departments findByDepartmentId(String departmentId);
}
