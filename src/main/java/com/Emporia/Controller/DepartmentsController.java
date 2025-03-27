package com.Emporia.Controller;

import com.Emporia.Configuration.ConstantConfiguration;
import com.Emporia.Model.DepartmentsModel;
import com.Emporia.Model.EmployeesModel;
import com.Emporia.Service.DepartmentsService;
import org.springframework.beans.factory.annotation.Autowired;
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

}
