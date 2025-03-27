package com.Emporia.Service;

import com.Emporia.Entity.Departments;
import com.Emporia.Entity.Employees;
import com.Emporia.GeneratorClass.VariablesGenerators;
import com.Emporia.Model.DepartmentsModel;
import com.Emporia.Model.EmployeesModel;
import com.Emporia.Model.GenericDataModel;
import com.Emporia.Repository.DepartmentsRepository;
import com.Emporia.Repository.EmployeesRepository;
import com.Emporia.Exception.BadRequestException;
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
public class EmployeesService {

    @Autowired
    private EmployeesRepository employeesRepository;

    @Autowired
    private DepartmentsRepository departmentsRepository;

    @Autowired
    private VariablesGenerators variablesGenerators;

    @Transactional(rollbackOn = Exception.class)
    public EmployeesModel addEmployeesDetails(EmployeesModel employeesModel) {
        log.info("***** add employee details *****");
        validateEmployeeModel(employeesModel);

        String employeeId = generateUniqueEmployeeId(employeesModel.getRole());
        employeesModel.setEmployeeId(employeeId);

        validateDuplicateEntries(employeesModel, null);
        setDepartment(employeesModel);
        setReportingManager(employeesModel);

        Employees employees = mapToEmployeeEntity(employeesModel, employeeId);
        employeesRepository.save(employees);

        log.info("***** Employee added successfully with employee id : {} *****", employeeId);
        return employeesModel;
    }

    @Transactional(rollbackOn = Exception.class)
    public EmployeesModel updateEmployeesDetails(EmployeesModel employeesModel, String employeeId) {
        log.info("***** update employee details *****");
        validateEmployeeModel(employeesModel);

        boolean isDepartmentChange = false;
        boolean isReportingManagerChange = false;

        employeesModel.setEmployeeId(employeeId);

        Employees employeeDetails = employeesRepository.findByEmployeeId(employeeId);
        if (employeeDetails == null) {
            throw new BadRequestException("Invalid Employee ID", "No employee found with ID: " + employeeId);
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

        if (employeeDetails.getReportingManagerId() != null && employeesModel.getReportingManager() != null &&
                employeesModel.getReportingManager().getEmployeeId() != null &&
                !employeeDetails.getReportingManagerId().equalsIgnoreCase(employeesModel.getReportingManager().getEmployeeId())) {

            setReportingManager(employeesModel);
            isReportingManagerChange = employeesModel.getReportingManager() != null;
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
        if (!isNullOrEmpty(employeesModel.getRole())) {
            employeeDetails.setRole(employeesModel.getRole());
        }

        List<String> validGenders = Arrays.asList("MALE", "FEMALE", "TRANSGENDER", "OTHER");
        if (employeesModel.getGender() != null && validGenders.contains(employeesModel.getGender().toUpperCase())) {
            employeeDetails.setGender(employeesModel.getGender().toUpperCase());
        }
        if (isDepartmentChange && employeesModel.getDepartment() != null && !isNullOrEmpty(employeesModel.getDepartment().getDepartmentId())) {
            employeeDetails.setDepartmentId(employeesModel.getDepartment().getDepartmentId());
        }
        if (isReportingManagerChange && employeesModel.getReportingManager() != null && !isNullOrEmpty(employeesModel.getReportingManager().getEmployeeId())) {
            employeeDetails.setReportingManagerId(employeesModel.getReportingManager().getEmployeeId());
        }
        employeesRepository.save(employeeDetails);

        log.info("***** Employee updated successfully with employee id : {} *****", employeeId);
        return employeesModel;
    }

    @Transactional(rollbackOn = Exception.class)
    public GenericDataModel assignEmployeeDepartments(GenericDataModel genericDataModel) {
        if (genericDataModel == null) {
            throw new BadRequestException("Invalid Request", "Data model cannot be null.");
        }

        Map<String, List<String>> departmentEmployeeMap = genericDataModel.getDepartmentEmployeesList();
        if (departmentEmployeeMap == null || departmentEmployeeMap.isEmpty()) {
            throw new BadRequestException("Invalid Data", "Department to Employees mapping cannot be empty.");
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

    public Page<EmployeesModel> getAllEmployeesDetails(PageRequest pageRequest) {
        Page<Employees> employeesList = employeesRepository.findAll(pageRequest);

        List<String> departmentIds = employeesList.stream()
                .map(Employees::getDepartmentId)
                .distinct()
                .collect(Collectors.toList());

        List<String> reportingManagerIds = employeesList.stream()
                .map(Employees::getReportingManagerId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Employees> reportingManagerMap = employeesRepository.findAllByEmployeeIdIn(reportingManagerIds)
                .stream()
                .collect(Collectors.toMap(
                        Employees::getEmployeeId,
                        emp -> emp,
                        (existing, replacement) -> existing
                ));

        Map<String, Departments> departmentsMap = departmentsRepository.findAllByDepartmentIdIn(departmentIds)
                .stream()
                .collect(Collectors.toMap(
                        Departments::getDepartmentId,
                        dept -> dept,
                        (existing, replacement) -> existing
                ));

        List<String> departmentHeadIds = departmentsMap.values().stream()
                .map(Departments::getDepartmentHeadId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Employees> departmentHeadsMap = employeesRepository.findAllByEmployeeIdIn(departmentHeadIds)
                .stream()
                .collect(Collectors.toMap(
                        Employees::getEmployeeId,
                        emp -> emp,
                        (existing, replacement) -> existing
                ));

        return employeesList.map(employee -> {
            EmployeesModel employeesModel = mapToEmployeeModel(employee);

            Departments department = departmentsMap.get(employee.getDepartmentId());
            if (department != null) {
                DepartmentsModel departmentsModel = mapToDepartmentModel(department);
                Employees departmentHead = departmentHeadsMap.get(department.getDepartmentHeadId());

                if (departmentHead != null) {
                    departmentsModel.setDepartmentHead(mapToEmployeeModel(departmentHead));
                }

                employeesModel.setDepartment(departmentsModel);
            }

            Employees reportingManager = reportingManagerMap.get(employee.getReportingManagerId());
            if (reportingManager != null) {
                EmployeesModel reportingManagerModel = mapToEmployeeModel(reportingManager);
                employeesModel.setReportingManager(reportingManagerModel);
            }
            return employeesModel;
        });
    }

    public EmployeesModel getEmployeesDetailsByEmployeeId(String employeeId) {
        Employees employees = employeesRepository.findByEmployeeId(employeeId);
        if (employees == null) {
            throw new BadRequestException("Invalid Employee ID", "No employee found with ID: " + employeeId);
        }
        EmployeesModel employeesModel = mapToEmployeeModel(employees);

        Departments departments = departmentsRepository.findByDepartmentId(employees.getDepartmentId());
        if (departments != null) {
            DepartmentsModel departmentsModel = mapToDepartmentModel(departments);

            Employees departmentHeads = employeesRepository.findByEmployeeId(departments.getDepartmentHeadId());
            if (departmentHeads != null) {
                departmentsModel.setDepartmentHead(mapToEmployeeModel(departmentHeads));
            }

            employeesModel.setDepartment(departmentsModel);
        }

        Employees reportingManager = employeesRepository.findByEmployeeId(employees.getReportingManagerId());
        if (reportingManager != null) {
            EmployeesModel reportingManagerModel = mapToEmployeeModel(reportingManager);
            employeesModel.setReportingManager(reportingManagerModel);
        }
        return employeesModel;
    }

    public List<EmployeesModel> getEmployeesNameAndEmployeesId(boolean lookup) {
        List<Employees> employeesList = employeesRepository.findAll();

        List<String> departmentsIds = employeesList.stream()
                .map(Employees::getDepartmentId)
                .distinct()
                .collect(Collectors.toList());

        List<String> reportingManagerIds = employeesList.stream()
                .map(Employees::getReportingManagerId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Employees> reportingManagerMap = employeesRepository.findAllByEmployeeIdIn(reportingManagerIds)
                .stream()
                .collect(Collectors.toMap(
                        Employees::getEmployeeId,
                        emp -> emp,
                        (existing, replacement) -> existing
                ));

        Map<String, Departments> departmentsMap = departmentsRepository.findAllByDepartmentIdIn(departmentsIds)
                .stream()
                .collect(Collectors.toMap(
                        Departments::getDepartmentId,
                        dept -> dept,
                        (existing, replacement) -> existing
                ));

        List<String> departmentsHeadIds = departmentsMap.values().stream()
                .map(Departments::getDepartmentHeadId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Employees> departmentHeadsMap = employeesRepository.findAllByEmployeeIdIn(departmentsHeadIds)
                .stream()
                .collect(Collectors.toMap(
                        Employees::getEmployeeId,
                        emp -> emp,
                        (existing, replacement) -> existing
                ));

        return employeesList.stream().map(employee -> {
            EmployeesModel employeesModel = new EmployeesModel();
            if (lookup) {
                employeesModel.setFirstName(employee.getFirstName());
                employeesModel.setLastName(employee.getLastName());
                employeesModel.setFullName(employee.getFullName());
                employeesModel.setEmployeeId(employee.getEmployeeId());
            } else {
                employeesModel = mapToEmployeeModel(employee);

                Departments department = departmentsMap.get(employee.getDepartmentId());
                if (department != null) {
                    DepartmentsModel departmentsModel = mapToDepartmentModel(department);
                    Employees departmentHead = departmentHeadsMap.get(department.getDepartmentHeadId());

                    if (departmentHead != null) {
                        departmentsModel.setDepartmentHead(mapToEmployeeModel(departmentHead));
                    }

                    employeesModel.setDepartment(departmentsModel);
                }

                Employees reportingManager = reportingManagerMap.get(employee.getReportingManagerId());
                if (reportingManager != null) {
                    EmployeesModel reportingManagerModel = mapToEmployeeModel(reportingManager);
                    employeesModel.setReportingManager(reportingManagerModel);
                }
            }
            return employeesModel;
        }).collect(Collectors.toList());
    }

    @Transactional(rollbackOn = Exception.class)
    public EmployeesModel updateEmployeeDepartment(String employeeId, String departmentId) {
        Employees employees = employeesRepository.findByEmployeeId(employeeId);
        if (employees == null) {
            throw new BadRequestException("Invalid Employee ID", "No employee found with ID: " + employeeId);
        }

        Departments departments = departmentsRepository.findByDepartmentId(departmentId);
        if (departments == null) {
            throw new BadRequestException("Invalid Department ID", "No department found with ID : " + departmentId);
        }

        employees.setDepartmentId(departments.getDepartmentId());
        employeesRepository.save(employees);

        EmployeesModel employeesModel = mapToEmployeeModel(employees);
        employeesModel.setDepartment(mapToDepartmentModel(departments));
        return employeesModel;
    }

    @Transactional(rollbackOn = Exception.class)
    public GenericDataModel assignEmployeeReportingManager(GenericDataModel genericDataModel) {
        if (genericDataModel == null) {
            throw new BadRequestException("Invalid Request", "Data model cannot be null.");
        }

        Map<String, List<String>> reportingManagerEmployeesMap = genericDataModel.getReportingManagerEmployeesList();
        if (reportingManagerEmployeesMap == null || reportingManagerEmployeesMap.isEmpty()) {
            throw new BadRequestException("Invalid Data", "Reporting Manager to Employees mapping cannot be empty.");
        }

        List<String> reportingManagersIds = new ArrayList<>(reportingManagerEmployeesMap.keySet());
        List<String> employeeIds = reportingManagerEmployeesMap.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        List<Employees> employees = employeesRepository.findAllByEmployeeIdIn(employeeIds);
        validateReportingManagerIsValid(reportingManagersIds);

        for (String reportingManagerId : reportingManagersIds) {
            List<String> assignedEmployeeIds = reportingManagerEmployeesMap.get(reportingManagerId);

            employees.stream()
                    .filter(employee -> assignedEmployeeIds.contains(employee.getEmployeeId()))
                    .forEach(employee -> employee.setReportingManagerId(reportingManagerId));
        }
        if (!employees.isEmpty())
            employeesRepository.saveAll(employees);

        return genericDataModel;
    }

    @Transactional(rollbackOn = Exception.class)
    public EmployeesModel updateEmployeeReportingManager(String employeeId, String reportingManagerId) {
        if (isNullOrEmpty(employeeId) || isNullOrEmpty(reportingManagerId)) {
            throw new BadRequestException("Invalid Input", "Employee ID or Reporting Manager ID cannot be null.");
        }

        List<String> employeeAndReportingManagerIds = Arrays.asList(employeeId, reportingManagerId);
        List<Employees> employeesList = employeesRepository.findAllByEmployeeIdIn(employeeAndReportingManagerIds);
        if (employeesList == null || employeesList.isEmpty()) {
            throw new BadRequestException("Invalid Employee IDs", "No employee found with the provided IDs: " + employeeAndReportingManagerIds);
        }

        Employees reportingManager = employeesList.stream()
                .filter(employee -> reportingManagerId.equalsIgnoreCase(employee.getEmployeeId()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Invalid Reporting Manager", "No reporting manager found with ID: " + reportingManagerId));

        Employees employeeDetails = employeesList.stream()
                .filter(employee -> employeeId.equalsIgnoreCase(employee.getEmployeeId()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Invalid Employee", "No employee found with ID: " + employeeId));

        employeeDetails.setReportingManagerId(reportingManager.getEmployeeId());
        employeesRepository.save(employeeDetails);

        EmployeesModel employeesModel = mapToEmployeeModel(employeeDetails);
        if (!isNullOrEmpty(employeeDetails.getDepartmentId())) {
            Departments department = departmentsRepository.findByDepartmentId(employeeDetails.getDepartmentId());
            if (department != null) {
                employeesModel.setDepartment(mapToDepartmentModel(department));
            }
        }
        return employeesModel;
    }

    private void validateReportingManagerIsValid(List<String> reportingManagersIds) {
        if (reportingManagersIds == null || reportingManagersIds.isEmpty()) {
            throw new BadRequestException("Invalid Manager IDs", "Reporting manager IDs cannot be null or empty.");
        }

        List<Employees> reportingManagers = employeesRepository.findAllByEmployeeIdIn(reportingManagersIds);
        if (reportingManagers == null || reportingManagers.isEmpty()) {
            throw new BadRequestException("Invalid Manager IDs", "No valid reporting managers found.");
        }

        Set<String> existingManagerIds = reportingManagers.stream()
                .map(Employees::getEmployeeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (!existingManagerIds.containsAll(reportingManagersIds)) {
            throw new BadRequestException("Invalid Manager IDs", "Some reporting managers do not exist.");
        }

        List<String> departmentIds = reportingManagers.stream()
                .map(Employees::getDepartmentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (departmentIds.isEmpty()) {
            throw new BadRequestException("Invalid Department IDs", "No valid department IDs found for reporting managers.");
        }

        List<Departments> departmentsList = departmentsRepository.findAllByDepartmentIdIn(departmentIds);
        if (departmentsList == null || departmentsList.isEmpty()) {
            throw new BadRequestException("Invalid Department IDs", "No valid departments found.");
        }

        Set<String> existingDepartmentIds = departmentsList.stream()
                .map(Departments::getDepartmentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (!existingDepartmentIds.containsAll(departmentIds)) {
            throw new BadRequestException("Invalid Department IDs", "Some departments do not exist.");
        }
    }

    public EmployeesModel mapToEmployeeModel(Employees employee) {
        EmployeesModel model = new EmployeesModel();
        model.setEmployeeId(employee.getEmployeeId());
        model.setFirstName(employee.getFirstName());
        model.setLastName(employee.getLastName());
        model.setFullName(employee.getFullName());
        model.setGender(employee.getGender());
        model.setEmail(employee.getEmail());
        model.setPhoneNumber(employee.getPhoneNumber());
        model.setSalary(employee.getSalary());
        model.setRole(employee.getRole());
        model.setDateOfBirth(employee.getDateOfBirth());
        model.setDateOfJoining(employee.getDateOfJoining());
        model.setYearlyBonusPercentage(employee.getYearlyBonusPercentage());
        return model;
    }

    public DepartmentsModel mapToDepartmentModel(Departments department) {
        DepartmentsModel model = new DepartmentsModel();
        model.setDepartmentId(department.getDepartmentId());
        model.setDepartmentName(department.getDepartmentName());
        model.setDescription(department.getDescription());
        model.setCreationDate(department.getCreatedAt());
        return model;
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
        if (employeesModel == null || employeesModel.getDepartment() == null) {
            return;
        }

        String departmentId = employeesModel.getDepartment().getDepartmentId();
        if (isNullOrEmpty(departmentId)) {
            return;
        }

        Departments departments = departmentsRepository.findByDepartmentId(departmentId);
        if (departments == null) {
            throw new BadRequestException("Invalid Department", "Department with ID " + departmentId + " does not exist.");
        }

        DepartmentsModel departmentsModel = new DepartmentsModel();
        departmentsModel.setDepartmentId(departments.getDepartmentId());
        departmentsModel.setDepartmentName(departments.getDepartmentName());

        employeesModel.setDepartment(departmentsModel);
    }

    private void setReportingManager(EmployeesModel employeesModel) {
        if (employeesModel == null || employeesModel.getReportingManager() == null) {
            return;
        }

        String reportingManagerId = employeesModel.getReportingManager().getEmployeeId();
        if (isNullOrEmpty(reportingManagerId)) {
            return;
        }
        Employees reportingManager = employeesRepository.findByEmployeeId(reportingManagerId);
        if (reportingManager == null) {
            throw new BadRequestException("Invalid Reporting Manager", "Reporting Manager with ID " + reportingManagerId + " does not exist.");
        }

        EmployeesModel reportingManagerModel = mapToEmployeeModel(reportingManager);
        employeesModel.setReportingManager(reportingManagerModel);
    }

    private Employees mapToEmployeeEntity(EmployeesModel employeesModel, String employeeId) {
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

        if (employeesModel.getDepartment() != null && !isNullOrEmpty(employeesModel.getDepartment().getDepartmentId())) {
            employees.setDepartmentId(employeesModel.getDepartment().getDepartmentId());
        }

        if (employeesModel.getReportingManager() != null && !isNullOrEmpty(employeesModel.getReportingManager().getEmployeeId())) {
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
