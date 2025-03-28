package com.Emporia.Controller;

import com.Emporia.Configuration.ConstantConfiguration;
import com.Emporia.Model.EmployeesModel;
import com.Emporia.Model.GenericDataModel;
import com.Emporia.Service.EmployeesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = ConstantConfiguration.EMPLOYEES_ROUTE)
public class EmployeesController {

    @Autowired
    private EmployeesService employeesService;

    @PostMapping(value = "/create")
    public EmployeesModel createEmployee(@RequestBody EmployeesModel employeesData) {
        return employeesService.addEmployeesDetails(employeesData);
    }

    @PutMapping(value = "/update/{employeeId}")
    public EmployeesModel updateEmployee(@PathVariable String employeeId, @RequestBody EmployeesModel employeesData) {
        return employeesService.updateEmployeesDetails(employeesData, employeeId);
    }

    @GetMapping()
    public Page<EmployeesModel> getAllEmployeesDetails(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return employeesService.getAllEmployeesDetails(pageRequest);
    }

    @GetMapping(value = "/{employeeId}")
    public EmployeesModel getEmployeesDetailsByEmployeeId(@PathVariable String employeeId) {
        return employeesService.getEmployeesDetailsByEmployeeId(employeeId);
    }

    @GetMapping("/get-name/lookup")
    public List<EmployeesModel> getEmployeesNameAndEmployeesId(@RequestParam(value = "lookup", defaultValue = "false") boolean lookup) {
        return employeesService.getEmployeesNameAndEmployeesId(lookup);
    }

    @PutMapping("/{employeeId}/departments/{departmentId}")
    public EmployeesModel updateEmployeeDepartment(@PathVariable String employeeId, @PathVariable String departmentId) {
        return employeesService.updateEmployeeDepartment(employeeId, departmentId);
    }

    @PutMapping("/assign-multiple-departments")
    public GenericDataModel updateEmployeeDepartment(@RequestBody GenericDataModel genericDataModel) {
        return employeesService.assignEmployeeDepartments(genericDataModel);
    }

    @PutMapping("/assign-multi-reporting-manager")
    public GenericDataModel assignEmployeeReportingManager(@RequestBody GenericDataModel genericDataModel) {
        return employeesService.assignEmployeeReportingManager(genericDataModel);
    }

    @PutMapping("/{employeeId}/update-reporting-manager/{reportingManagerId}")
    public EmployeesModel updateEmployeeReportingManager(@PathVariable String employeeId, @PathVariable String reportingManagerId) {
        return employeesService.updateEmployeeReportingManager(employeeId, reportingManagerId);
    }

    @DeleteMapping("/{employeeId}")
    public String deleteEmployeeId(@PathVariable String employeeId) {
        return employeesService.deleteEmployeesDetailsByEmployeesId(employeeId);
    }

}
