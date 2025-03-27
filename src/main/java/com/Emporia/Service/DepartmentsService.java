package com.Emporia.Service;

import com.Emporia.Configuration.CustomObjectMapper;
import com.Emporia.Entity.Departments;
import com.Emporia.Entity.Employees;
import com.Emporia.Exception.BadRequestException;
import com.Emporia.GeneratorClass.VariablesGenerators;
import com.Emporia.Model.DepartmentsModel;
import com.Emporia.Model.EmployeesModel;
import com.Emporia.Repository.DepartmentsRepository;
import com.Emporia.Repository.EmployeesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Slf4j
public class DepartmentsService {

    @Autowired
    private DepartmentsRepository departmentsRepository;

    @Autowired
    private EmployeesRepository employeesRepository;

    @Autowired
    private EmployeesService employeesService;

    @Autowired
    private VariablesGenerators variablesGenerators;

    static CustomObjectMapper customObjectMapper = new CustomObjectMapper();

    @Transactional(rollbackOn = Exception.class)
    public DepartmentsModel addDepartmentsDetails(DepartmentsModel departmentsModel) {
        log.info("***** Adding department details *****");
        validateDepartmentsModel(departmentsModel);

        String departmentId = generateUniqueDepartmentId(departmentsModel.getDepartmentName());
        departmentsModel.setDepartmentId(departmentId);

        validateDuplicateEntries(departmentsModel);
        setDepartmentHead(departmentsModel);

        Departments departments = mapToDepartmentsEntity(departmentsModel, departmentId, null);
        departmentsRepository.save(departments);

        log.info("***** Department added successfully with department id : {} *****", departmentId);
        return departmentsModel;
    }

    @Transactional(rollbackOn = Exception.class)
    public DepartmentsModel updateDepartmentsDetails(DepartmentsModel departmentsModel, String departmentId) {
        log.info("***** Updating departments details *****");
        validateDepartmentsModel(departmentsModel);

        Departments departments = departmentsRepository.findByDepartmentId(departmentId);
        if (departments == null) {
            throw new BadRequestException("Invalid Department", "Department with ID " + departmentId + " not found.");
        }

        String departmentName = departmentsModel.getDepartmentName();
        if (!isNullOrEmpty(departmentName) && !departments.getDepartmentName().equalsIgnoreCase(departmentName)) {
            validateDuplicateEntries(departmentsModel);
        }

        setDepartmentHead(departmentsModel);
        departments = mapToDepartmentsEntity(departmentsModel, departmentId, departments);
        departmentsRepository.save(departments);

        log.info("***** Department updated successfully with department id : {} *****", departmentId);
        return departmentsModel;
    }

    private Departments mapToDepartmentsEntity(DepartmentsModel departmentsModel, String departmentId, Departments existingDepartment) {
        if (existingDepartment == null) {
            existingDepartment = new Departments();
            existingDepartment.setDepartmentId(departmentId);
            existingDepartment.setDepartmentName(departmentsModel.getDepartmentName());
        }

        existingDepartment.setDescription(customObjectMapper.writeAsString(departmentsModel.getDescription()));

        if (departmentsModel.getDepartmentHead() != null && !isNullOrEmpty(departmentsModel.getDepartmentHead().getEmployeeId())) {
            existingDepartment.setDepartmentHeadId(departmentsModel.getDepartmentHead().getEmployeeId());
        }

        return existingDepartment;
    }

    private void setDepartmentHead(DepartmentsModel departmentsModel) {
        if (departmentsModel == null || departmentsModel.getDepartmentHead() == null) {
            return;
        }

        String departmentHeadId = departmentsModel.getDepartmentHead().getEmployeeId();
        if (isNullOrEmpty(departmentHeadId)) {
            return;
        }

        Employees reportingManager = employeesRepository.findByEmployeeId(departmentHeadId);
        if (reportingManager == null) {
            throw new BadRequestException("Invalid Department Head", "Department Head with ID " + departmentHeadId + " does not exist.");
        }

        EmployeesModel reportingManagerModel = employeesService.mapToEmployeeModel(reportingManager);
        departmentsModel.setDepartmentHead(reportingManagerModel);
    }

    private void validateDuplicateEntries(DepartmentsModel departmentsModel) {
        if (departmentsRepository.existsByDepartmentName(departmentsModel.getDepartmentName())) {
            throw new BadRequestException("Duplicate Department Name", "Department Name already exists.");
        }
    }

    private String generateUniqueDepartmentId(String departmentName) {
        String departmentId;
        do {
            departmentId = variablesGenerators.generateDepartmentId(departmentName);
        } while (departmentsRepository.existsByDepartmentId(departmentId));
        return departmentId;
    }

    private void validateDepartmentsModel(DepartmentsModel departmentsModel) {
        if (departmentsModel == null) {
            throw new BadRequestException("departments data", "Departments Data cannot be null");
        }

        if (isNullOrEmpty(departmentsModel.getDepartmentName())) {
            throw new BadRequestException("Department Name", "Department Name is required");
        }
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

}
