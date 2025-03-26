package com.Emporia.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GenericDataModel {

    @JsonProperty(value = "employee_id")
    private String employeeId;

    @JsonProperty(value = "department_id")
    private String departmentId;

    @JsonProperty(value = "department_employees_list")
    private Map<String, List<String>> departmentEmployeesList;

}
