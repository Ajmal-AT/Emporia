package com.Emporia.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.Column;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmployeesModel {

    @JsonProperty(value = "employee_id")
    private String employeeId;

    @JsonProperty(value = "first_name")
    private String firstName;

    @JsonProperty(value = "last_name")
    private String lastName;

    @JsonProperty(value = "full_name")
    private String fullName;

    @JsonProperty(value = "salary")
    private BigDecimal salary = BigDecimal.ZERO;

    @JsonProperty(value = "date_of_joining")
    private LocalDate dateOfJoining;

    @JsonProperty(value = "date_of_birth")
    private LocalDate dateOfBirth;

    @JsonProperty(value = "email")
    private String email;

    @JsonProperty(value = "phone_number")
    private String phoneNumber;

    @JsonProperty(value = "yearly_bonus_percentage")
    private BigDecimal yearlyBonusPercentage = BigDecimal.ZERO;

    @JsonProperty(value = "role")
    private String role;

    @JsonProperty(value = "gender")  // Gender options: MALE, FEMALE, TRANSGENDER, OTHER
    private String gender;

    @JsonProperty(value = "department")
    private DepartmentsModel department;

}
