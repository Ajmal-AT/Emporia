package com.Emporia.Service;

import com.Emporia.Entity.Departments;
import com.Emporia.Entity.Employees;
import com.Emporia.GeneratorClass.VariablesGenerators;
import com.Emporia.Model.DepartmentsModel;
import com.Emporia.Model.EmployeesModel;
import com.Emporia.Model.GenericDataModel;
import com.Emporia.Repository.DepartmentsRepository;
import com.Emporia.Repository.EmployeesRepository;
import com.Emporia.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

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
        log.info("***** add employee details *****");
        validateEmployeeModel(employeesModel);

        String employeeId = generateUniqueEmployeeId(employeesModel.getRole());
        employeesModel.setEmployeeId(employeeId);

        validateDuplicateEntries(employeesModel, null);
        setDepartment(employeesModel);

        Employees employees = mapToEntity(employeesModel, employeeId);
        employeesRepository.save(employees);

        log.info("***** Employee added successfully with employee id : {} *****", employeeId);
        return employeesModel;
    }

    @Transactional(rollbackOn = Exception.class)
    public EmployeesModel updateEmployeesDetails(EmployeesModel employeesModel, String employeeId) {
        log.info("***** update employee details *****");
        validateEmployeeModel(employeesModel);

        boolean isDepartmentChange = false;
        employeesModel.setEmployeeId(employeeId);

        Employees employeeDetails = employeesRepository.findByEmployeeId(employeeId);
        if (employeeDetails == null) {
            throw new BadRequestException("", "");
        }
        employeesModel.setEmployeeId(employeeDetails.getEmployeeId());

        if (!employeeDetails.getEmail().equalsIgnoreCase(employeesModel.getEmail()) || !employeeDetails.getPhoneNumber().equalsIgnoreCase(employeesModel.getPhoneNumber())) {
            validateDuplicateEntries(employeesModel, employeeDetails);
            employeeDetails.setEmail(employeesModel.getEmail());
            employeeDetails.setPhoneNumber(employeesModel.getPhoneNumber());
        }
        if (employeeDetails.getDepartmentId() != null && employeesModel.getDepartment() != null &&
                employeesModel.getDepartment().getDepartmentId() != null &&
                !employeeDetails.getDepartmentId().equalsIgnoreCase(employeesModel.getDepartment().getDepartmentId())) {

            setDepartment(employeesModel);
            isDepartmentChange = employeesModel.getDepartment() != null;
        }

        log.info("***** update employee details with employee id : {} || isDepartmentChange : {} *****", employeeId, isDepartmentChange);
        if (employeesModel.getSalary() != null) {
            employeeDetails.setSalary(employeesModel.getSalary());
        }
        if (employeesModel.getFirstName() != null) {
            employeeDetails.setFirstName(employeesModel.getFirstName());
        }
        if (employeesModel.getLastName() != null) {
            employeeDetails.setLastName(employeesModel.getLastName());
        }
        if (employeesModel.getDateOfBirth() != null) {
            employeeDetails.setDateOfBirth(employeesModel.getDateOfBirth());
        }
        if (employeesModel.getYearlyBonusPercentage() != null) {
            employeeDetails.setYearlyBonusPercentage(employeesModel.getYearlyBonusPercentage());
        }
        if (employeesModel.getRole() != null) {
            employeeDetails.setRole(employeesModel.getRole());
        }

        List<String> validGenders = Arrays.asList("MALE", "FEMALE", "TRANSGENDER", "OTHER");
        if (employeesModel.getGender() != null && validGenders.contains(employeesModel.getGender().toUpperCase())) {
            employeeDetails.setGender(employeesModel.getGender().toUpperCase());
        }
        if (isDepartmentChange && employeesModel.getDepartment() != null && employeesModel.getDepartment().getDepartmentId() != null) {
            employeeDetails.setDepartmentId(employeesModel.getDepartment().getDepartmentId());
        }
        employeesRepository.save(employeeDetails);

        log.info("***** Employee updated successfully with employee id : {} *****", employeeId);
        return employeesModel;
    }

    @Transactional(rollbackOn = Exception.class)
    public GenericDataModel updateEmployeeDepartments(GenericDataModel genericDataModel) {
        if (genericDataModel == null) {
            throw new BadRequestException("Invalid Request", "Data model cannot be null.");
        }

        Map<String, List<String>> departmentEmployeeMap = genericDataModel.getDepartmentEmployeesList();
        if (departmentEmployeeMap == null || departmentEmployeeMap.isEmpty()) {
            throw new BadRequestException("Invalid Data", "Department to Employee mapping cannot be empty.");
        }
        List<String> departmentIds = new ArrayList<>(departmentEmployeeMap.keySet());
        List<String> employeeIds = departmentEmployeeMap.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        List<Departments> departments = departmentsRepository.findAllByDepartmentIdIn(departmentIds);
        List<Employees> employees = employeesRepository.findAllByEmployeeIdIn(employeeIds);

        for (Departments department : departments) {
            List<String> assignedEmployeeIds = departmentEmployeeMap.get(department.getDepartmentId());

            employees.stream()
                    .filter(employee -> assignedEmployeeIds.contains(employee.getEmployeeId()))
                    .forEach(employee -> employee.setDepartmentId(department.getDepartmentId()));
        }
        if (!employees.isEmpty())
            employeesRepository.saveAll(employees);

        return genericDataModel;
    }

    private String generateUniqueEmployeeId(String role) {
        String employeeId;
        do {
            employeeId = variablesGenerators.generateEmployeeId(role);
        } while (employeesRepository.existsByEmployeeId(employeeId));
        return employeeId;
    }

    private void validateDuplicateEntries(EmployeesModel employeesModel, Employees employeeDetails) {
        boolean isEmailChanged = employeeDetails != null && !employeeDetails.getEmail().equalsIgnoreCase(employeesModel.getEmail());
        boolean isPhoneChanged = employeeDetails != null && !employeeDetails.getPhoneNumber().equalsIgnoreCase(employeesModel.getPhoneNumber());

        if (isEmailChanged && employeesRepository.existsByEmail(employeesModel.getEmail())) {
            throw new BadRequestException("Duplicate Email", "Email already exists.");
        }
        if (isPhoneChanged && employeesRepository.existsByPhoneNumber(employeesModel.getPhoneNumber())) {
            throw new BadRequestException("Duplicate Phone Number", "Phone number already exists.");
        }
    }

    private void setDepartment(EmployeesModel employeesModel) {
        String departmentId = employeesModel.getDepartment() != null
                ? employeesModel.getDepartment().getDepartmentId()
                : null;

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
