package com.Emporia.Model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import java.time.LocalDateTime;

@Data
public class DepartmentsModel {

    @JsonProperty(value = "department_id")
    private String departmentId;

    @JsonProperty(value = "department_name")
    private String departmentName;

    @JsonProperty(value = "description")
    private String description;

    @JsonProperty(value = "department_head")
    private EmployeesModel departmentHead;

    @JsonProperty(value = "creation_date")
    private LocalDateTime creationDate;

}
