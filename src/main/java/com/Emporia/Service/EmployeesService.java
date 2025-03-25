package com.Emporia.Service;

import com.Emporia.Entity.Departments;
import com.Emporia.Entity.Employees;
import com.Emporia.GeneratorClass.VariablesGenerators;
import com.Emporia.Model.DepartmentsModel;
import com.Emporia.Model.EmployeesModel;
import com.Emporia.Repository.DepartmentsRepository;
import com.Emporia.Repository.EmployeesRepository;
import com.Emporia.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class EmployeesService {

    @Autowired
    EmployeesRepository employeesRepository;

    @Autowired
    DepartmentsRepository departmentsRepository;

    @Autowired
    VariablesGenerators variablesGenerators;

    @Transactional(rollbackOn = Exception.class)
    public EmployeesModel addEmployeesDetails(EmployeesModel employeesModel) {
        log.info("***** employee details *****");
        validateEmployeeModel(employeesModel);

        String employeeId = generateUniqueEmployeeId(employeesModel.getRole());
        employeesModel.setEmployeeId(employeeId);

        validateDuplicateEntries(employeesModel);
        setReportingManagerAndDepartment(employeesModel);

        Employees employees = mapToEntity(employeesModel, employeeId);
        employeesRepository.save(employees);

        log.info("***** Employee added successfully with ID: {} *****", employeeId);
        return employeesModel;
    }

    private String generateUniqueEmployeeId(String role) {
        String employeeId;
        do {
            employeeId = variablesGenerators.generateEmployeeId(role);
        } while (employeesRepository.existsByEmployeeId(employeeId));
        return employeeId;
    }

    private void validateDuplicateEntries(EmployeesModel employeesModel) {
        if (employeesRepository.existsByPhoneNumber(employeesModel.getPhoneNumber())) {
            throw new BadRequestException("Duplicate Phone Number", "Phone number already exists.");
        }
        if (employeesRepository.existsByEmail(employeesModel.getEmail())) {
            throw new BadRequestException("Duplicate Email", "Email already exists.");
        }
    }

    private void setReportingManagerAndDepartment(EmployeesModel employeesModel) {
        String reportingManagerId = employeesModel.getReportingManager() != null
                ? employeesModel.getReportingManager().getEmployeeId()
                : null;
        String departmentId = employeesModel.getDepartment() != null
                ? employeesModel.getDepartment().getDepartmentId()
                : null;

        if (reportingManagerId != null) {
            Employees manager = employeesRepository.findByEmployeeId(reportingManagerId);
            if (manager != null) {
                Departments department = departmentsRepository.findByDepartmentId(manager.getDepartmentId());
                DepartmentsModel departmentsModel = null;

                if (department != null) {
                    departmentsModel = new DepartmentsModel();
                    departmentsModel.setDepartmentId(department.getDepartmentId());
                    departmentsModel.setDepartmentName(department.getDepartmentName());
                }

                EmployeesModel reportingManagerModel = new EmployeesModel();
                reportingManagerModel.setEmployeeId(manager.getEmployeeId());
                reportingManagerModel.setFullName(manager.getFullName());
                reportingManagerModel.setRole(manager.getRole());
                reportingManagerModel.setDepartment(departmentsModel);

                employeesModel.setReportingManager(reportingManagerModel);
            }
        }

        if (departmentId != null) {
            Departments department = departmentsRepository.findByDepartmentId(departmentId);
            if (department != null) {
                DepartmentsModel departmentsModel = new DepartmentsModel();
                departmentsModel.setDepartmentId(department.getDepartmentId());
                departmentsModel.setDepartmentName(department.getDepartmentName());
                employeesModel.setDepartment(departmentsModel);
            }
        }
    }


    private Employees mapToEntity(EmployeesModel employeesModel, String employeeId) {
        Employees employees = new Employees();
        employees.setEmployeeId(employeeId);
        employees.setFirstName(employeesModel.getFirstName());
        employees.setLastName(employeesModel.getLastName());
        employees.setSalary(employeesModel.getSalary());
        employees.setDateOfJoining(employeesModel.getDateOfJoining());
        employees.setDateOfBirth(employeesModel.getDateOfBirth());
        employees.setEmail(employeesModel.getEmail());
        employees.setPhoneNumber(employeesModel.getPhoneNumber());
        employees.setYearlyBonusPercentage(employeesModel.getYearlyBonusPercentage());
        employees.setRole(employeesModel.getRole());

        List<String> validGenders = Arrays.asList("MALE", "FEMALE", "TRANSGENDER", "OTHER");
        if (!validGenders.contains(employeesModel.getGender().toUpperCase())) {
            throw new BadRequestException("Invalid Gender", "Gender must be one of " + validGenders);
        }
        employees.setGender(employeesModel.getGender().toUpperCase());

        if (employeesModel.getDepartment() != null && employeesModel.getDepartment().getDepartmentId() != null) {
            employees.setDepartmentId(employeesModel.getDepartment().getDepartmentId());
        }

        if (employeesModel.getReportingManager() != null && employeesModel.getReportingManager().getEmployeeId() != null) {
            employees.setReportingManagerId(employeesModel.getReportingManager().getEmployeeId());
        }

        return employees;
    }

    private void validateEmployeeModel(EmployeesModel employeesModel) {
        if (employeesModel == null) {
            throw new BadRequestException("employees data", "Employees Data cannot be null");
        }

        if (isNullOrEmpty(employeesModel.getEmail())) {
            throw new BadRequestException("email", "Email is required");
        }

        if (isNullOrEmpty(employeesModel.getGender())) {
            throw new BadRequestException("gender", "Gender is required");
        }

        if (isNullOrEmpty(employeesModel.getRole())) {
            throw new BadRequestException("role", "Role is required");
        }

        if (isNullOrEmpty(employeesModel.getPhoneNumber())) {
            throw new BadRequestException("phone number", "Phone number is required");
        }

        if (isNullOrEmpty(employeesModel.getFirstName())) {
            throw new BadRequestException("firstName", "First name is required");
        }
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }


}
