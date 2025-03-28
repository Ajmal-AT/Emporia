package com.Emporia.Service;

import com.Emporia.Configuration.ConstantConfiguration;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

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

    private static final CustomObjectMapper customObjectMapper = new CustomObjectMapper();

    @Transactional(rollbackOn = Exception.class)
    public DepartmentsModel addDepartmentsDetails(DepartmentsModel departmentsModel) {
        log.info("***** Adding department details *****");
        validateDepartmentsModel(departmentsModel);

        String departmentId = generateUniqueDepartmentId(departmentsModel.getDepartmentName());
        departmentsModel.setDepartmentId(departmentId);

        validateDuplicateEntries(departmentsModel.getDepartmentName());
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

        if (!isNullOrEmpty(departmentsModel.getDepartmentName()) && !departments.getDepartmentName().equalsIgnoreCase(departmentsModel.getDepartmentName())) {
            validateDuplicateEntries(departmentsModel.getDepartmentName());
        }

        setDepartmentHead(departmentsModel);
        departments = mapToDepartmentsEntity(departmentsModel, departmentId, departments);
        departmentsRepository.save(departments);

        log.info("***** Department updated successfully with department id : {} *****", departmentId);
        return departmentsModel;
    }

    public Page<DepartmentsModel> getAllDepartmentsDetails(PageRequest pageRequest) {
        Page<Departments> departmentsPage = departmentsRepository.findAll(pageRequest);

        List<String> departmentHeadIds = departmentsPage.stream()
                .map(Departments::getDepartmentHeadId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Employees> departmentHeadMap = fetchEmployeesByIds(departmentHeadIds);

        return departmentsPage.map(departments -> mapToDepartmentsModel(departments, departmentHeadMap));
    }

    public DepartmentsModel getDepartmentsByDepartmentId(String departmentId, String expand) {
        List<String> expandChecks = Arrays.asList(ConstantConfiguration.EMPLOYEES, ConstantConfiguration.EMPLOYEE);

        if (!isNullOrEmpty(expand) && expandChecks.contains(expand.toUpperCase())) {
            return getDepartmentsDetailsByDepartmentId(departmentId, expand);
        } else {
            return getDepartmentsDetailsByDepartmentId(departmentId);
        }
    }

    public DepartmentsModel getDepartmentsDetailsByDepartmentId(String departmentId) {
        Departments departments = departmentsRepository.findByDepartmentId(departmentId);
        if (departments == null) {
            throw new BadRequestException("Invalid Department", "Department with ID " + departmentId + " not found.");
        }

        List<String> departmentHeadIds = departments.getDepartmentHeadId() != null
                ? Collections.singletonList(departments.getDepartmentHeadId())
                : Collections.emptyList();
        Map<String, Employees> departmentHeadMap = fetchEmployeesByIds(departmentHeadIds);

        return mapToDepartmentsModel(departments, departmentHeadMap);
    }

    public DepartmentsModel getDepartmentsDetailsByDepartmentId(String departmentId, String expand) {
        Departments departments = departmentsRepository.findByDepartmentId(departmentId);
        if (departments == null) {
            throw new BadRequestException("Invalid Department", "Department with ID " + departmentId + " not found.");
        }

        List<Employees> employeesList = new ArrayList<>();
        if (!isNullOrEmpty(departments.getDepartmentId())) {
            employeesList = employeesRepository.findAllByDepartmentId(departments.getDepartmentId());
        }

        List<String> departmentHeadIds = departments.getDepartmentHeadId() != null
                ? Collections.singletonList(departments.getDepartmentHeadId())
                : Collections.emptyList();
        Map<String, Employees> departmentHeadMap = fetchEmployeesByIds(departmentHeadIds);

        DepartmentsModel departmentsModel = mapToDepartmentsModel(departments, departmentHeadMap);
        if (!employeesList.isEmpty()) {
            List<EmployeesModel> employeesModelsList = employeesList.stream().map(employees -> employeesService.mapToEmployeeModel(employees)).collect(Collectors.toList());
            departmentsModel.setEmployees(employeesModelsList);
        }
        return departmentsModel;
    }

    public DepartmentsModel getDepartmentsDetailsByDepartmentName(String departmentName) {
        Departments departments = departmentsRepository.findByDepartmentName(departmentName);
        if (departments == null) {
            throw new BadRequestException("Invalid Department", "Department with Name " + departmentName + " not found.");
        }

        List<String> departmentHeadIds = departments.getDepartmentHeadId() != null
                ? Collections.singletonList(departments.getDepartmentHeadId())
                : Collections.emptyList();
        Map<String, Employees> departmentHeadMap = fetchEmployeesByIds(departmentHeadIds);

        return mapToDepartmentsModel(departments, departmentHeadMap);
    }

    @Transactional(rollbackOn = Exception.class)
    public DepartmentsModel assignDepartmentHead(String departmentId, String employeeId) {
        Employees departmentHead = employeesRepository.findByEmployeeId(employeeId);
        if (departmentHead == null) {
            throw new BadRequestException("Invalid Department Head", "No Department Head found with ID: " + employeeId);
        }

        Departments departments = departmentsRepository.findByDepartmentId(departmentId);
        if (departments == null) {
            throw new BadRequestException("Invalid Department", "Department with ID " + departmentId + " not found.");
        }

        departments.setDepartmentHeadId(departmentHead.getEmployeeId());
        departmentsRepository.save(departments);

        Map<String, Employees> departmentHeadMap = new HashMap<>();
        departmentHeadMap.put(departmentHead.getEmployeeId(), departmentHead);
        return mapToDepartmentsModel(departments, departmentHeadMap);
    }

    @Transactional(rollbackOn = Exception.class)
    public String deleteDepartmentsDetailsByDepartmentId(String departmentId) {
        Departments departments = departmentsRepository.findByDepartmentId(departmentId);
        if (departments == null) {
            throw new BadRequestException("Invalid Department", "Department with ID " + departmentId + " not found.");
        }

        if (!isNullOrEmpty(departments.getDepartmentHeadId())) {
            Employees departmentHead = employeesRepository.findByEmployeeId(departments.getDepartmentHeadId());
            if (departmentHead == null) {
                throw new BadRequestException("Invalid Department Head", "Department Head with ID " + departments.getDepartmentHeadId() + " not found.");
            }
        }

        List<Employees> employeesList = employeesRepository.findAllByDepartmentId(departments.getDepartmentId());
        if (!employeesList.isEmpty()) {
            employeesList.forEach(employee -> employee.setDepartmentId(null));
            employeesRepository.saveAll(employeesList);
        }

        departmentsRepository.deleteById(departments.getId());
        return "Department deleted successfully.";
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

    private DepartmentsModel mapToDepartmentsModel(Departments departments, Map<String, Employees> departmentHeadMap) {
        DepartmentsModel departmentsModel = new DepartmentsModel();
        departmentsModel.setDepartmentId(departments.getDepartmentId());
        departmentsModel.setDepartmentName(departments.getDepartmentName());
        departmentsModel.setDescription(departments.getDescription());


        if (!isNullOrEmpty(departments.getDepartmentHeadId())) {
            Employees departmentHead = departmentHeadMap.get(departments.getDepartmentHeadId());
            EmployeesModel departmentHeadModel = employeesService.mapToEmployeeModel(departmentHead);
            departmentsModel.setDepartmentHead(departmentHeadModel);
        }

        return departmentsModel;
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

    private void validateDuplicateEntries(String departmentName) {
        if (departmentsRepository.existsByDepartmentName(departmentName)) {
            throw new BadRequestException("Duplicate Department", "Department Name already exists: " + departmentName);
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

    private Map<String, Employees> fetchEmployeesByIds(List<String> departmentHeadIds) {
        return employeesRepository.findAllByEmployeeIdIn(departmentHeadIds)
                .stream()
                .collect(Collectors.toMap(
                        Employees::getEmployeeId,
                        emp -> emp,
                        (existing, replacement) -> existing
                ));
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

}
