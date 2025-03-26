package com.Emporia.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees")
@Data
public class Employees {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false, unique = true)
    @JsonProperty(value = "employee_id")
    private String employeeId;

    @Column(name = "first_name", nullable = false)
    @JsonProperty(value = "first_name")
    private String firstName;

    @Column(name = "last_name")
    @JsonProperty(value = "last_name")
    private String lastName;

    @Column(name = "salary")
    @JsonProperty(value = "salary")
    private BigDecimal salary = BigDecimal.ZERO;

    @Column(name = "date_of_joining")
    @JsonProperty(value = "date_of_joining")
    private LocalDate dateOfJoining;

    @Column(name = "date_of_birth")
    @JsonProperty(value = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "email", nullable = false, unique = true)
    @JsonProperty(value = "email")
    private String email;

    @Column(name = "phone_number", nullable = false, unique = true)
    @JsonProperty(value = "phone_number")
    private String phoneNumber;

    @Column(name = "yearly_bonus_percentage")
    @JsonProperty(value = "yearly_bonus_percentage")
    private BigDecimal yearlyBonusPercentage = BigDecimal.ZERO;

    @Column(name = "role", nullable = false)
    @JsonProperty(value = "role")
    private String role;

    @Column(name = "gender", nullable = false)
    @JsonProperty(value = "gender")  // Gender options: MALE, FEMALE, TRANSGENDER, OTHER
    private String gender;

    @Column(name = "department_id")
    @JsonProperty(value = "department_id")
    private String departmentId;

    @Column(name = "created_at", updatable = false)
    @JsonProperty(value = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @CreationTimestamp
    @Setter(value = AccessLevel.NONE)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    @JsonProperty(value = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @UpdateTimestamp
    @Setter(value = AccessLevel.NONE)
    private LocalDateTime updatedAt;

    public String getFullName() {
        return firstName + " " + lastName;
    }

}
