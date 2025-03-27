package com.Emporia.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "departments")
@Data
public class Departments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "department_id", nullable = false, unique = true)
    @JsonProperty(value = "department_id")
    private String departmentId;

    @Column(name = "department_name", nullable = false, unique = true)
    @JsonProperty(value = "department_name")
    private String departmentName;

    @Column(name = "description", columnDefinition = "TEXT")
    @JsonProperty(value = "description")
    private String description;

    @Column(name = "department_head_id")
    @JsonProperty(value = "department_head_id")
    private String departmentHeadId;

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

}
