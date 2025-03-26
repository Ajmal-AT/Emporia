package com.Emporia.GeneratorClass;

import com.Emporia.Exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class VariablesGenerators {

    public String generateEmployeeId(String role) {
        if (role == null || role.trim().isEmpty()) {
            throw new BadRequestException("Role cannot be null or empty", "Role cannot be null or empty");
        }

        String rolePrefix = role.length() >= 3 ? role.substring(0, 3).toUpperCase() : role.toUpperCase();

        int randomNumber = new Random().nextInt(9000) + 1000;

        return "EMP-" + rolePrefix + "-" + randomNumber;
    }

    public String generateDepartmentId(String departmentName) {
        if (departmentName == null || departmentName.trim().isEmpty()) {
            throw new BadRequestException("Department Name cannot be null or empty", "Department Name cannot be null or empty");
        }

        String departmentNamePrefix = departmentName.length() >= 4 ? departmentName.substring(0, 4).toUpperCase() : departmentName.toUpperCase();

        int randomNumber = new Random().nextInt(9000) + 1000;

        return "DEPT-" + departmentNamePrefix + "-" + randomNumber;
    }

}
