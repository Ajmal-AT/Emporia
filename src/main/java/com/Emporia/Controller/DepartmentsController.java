package com.Emporia.Controller;

import com.Emporia.Configuration.ConstantConfiguration;
import com.Emporia.Model.DepartmentsModel;
import com.Emporia.Model.EmployeesModel;
import com.Emporia.Service.DepartmentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = ConstantConfiguration.DEPARTMENTS_ROUTE)
public class DepartmentsController {

    @Autowired
    private DepartmentsService departmentsService;

    @PostMapping(value = "/create")
    public DepartmentsModel createDepartmentsDetails(@RequestBody DepartmentsModel departmentsModel) {
        return departmentsService.addDepartmentsDetails(departmentsModel);
    }

    @PutMapping(value = "/update/{departmentId}")
    public DepartmentsModel updateEmployee(@PathVariable String departmentId, @RequestBody DepartmentsModel departmentsData) {
        return departmentsService.updateDepartmentsDetails(departmentsData, departmentId);
    }

    @PutMapping(value = "/{departmentId}/assign-department-head/{employeeId}")
    public DepartmentsModel assignDepartmentHead(@PathVariable String departmentId, @PathVariable String employeeId) {
        return departmentsService.assignDepartmentHead(departmentId, employeeId);
    }

    @GetMapping()
    public Page<DepartmentsModel> getAllDepartments(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return departmentsService.getAllDepartmentsDetails(pageRequest);
    }

    @GetMapping("/{departmentId}")
    public DepartmentsModel getDepartmentByIdWithExpand(@PathVariable String departmentId, @RequestParam(value = "expand", defaultValue = "all") String expand) {
        return departmentsService.getDepartmentsByDepartmentId(departmentId, expand);
    }

    @DeleteMapping("/{departmentId}")
    public String deleteDepartment(@PathVariable String departmentId) {
        return departmentsService.deleteDepartmentsDetailsByDepartmentId(departmentId);
    }

}
