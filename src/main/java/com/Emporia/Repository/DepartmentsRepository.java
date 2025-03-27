package com.Emporia.Repository;

import com.Emporia.Entity.Departments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentsRepository extends JpaRepository<Departments, Long> {

    Departments findByDepartmentId(String departmentId);

    List<Departments> findAllByDepartmentIdIn(List<String> departmentIds);

    boolean existsByDepartmentId(String departmentId);

    boolean existsByDepartmentName(String departmentName);

}
