package com.connectsoar.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class CreateEmployeeAdminRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String department;
    private String designation;
    private String phone;
    private String address;

    @JsonProperty("adhar_number")
    @JsonAlias({"adharNumber", "adhar_number", "aadharNumber", "aadhar_number", "aadhaar_number", "adhar"})
    private String adharNumber;

    private String role; // "admin" or "employee", defaults to "employee"

    public CreateEmployeeAdminRequest() {
    }

    public CreateEmployeeAdminRequest(String name, String email, String department, String designation, String phone) {
        this(name, email, department, designation, phone, null, null, null);
    }

    public CreateEmployeeAdminRequest(String name, String email, String department, String designation,
                                      String phone, String address, String adharNumber, String role) {
        this.name = name;
        this.email = email;
        this.department = department;
        this.designation = designation;
        this.phone = phone;
        this.address = address;
        this.adharNumber = adharNumber;
        this.role = role;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String email;
        private String department;
        private String designation;
        private String phone;
        private String address;
        private String adharNumber;
        private String role;

        public Builder name(String name) { this.name = name; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder department(String department) { this.department = department; return this; }
        public Builder designation(String designation) { this.designation = designation; return this; }
        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder address(String address) { this.address = address; return this; }
        public Builder adharNumber(String adharNumber) { this.adharNumber = adharNumber; return this; }
        public Builder role(String role) { this.role = role; return this; }

        public CreateEmployeeAdminRequest build() {
            return new CreateEmployeeAdminRequest(name, email, department, designation, phone, address, adharNumber, role);
        }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getAdharNumber() { return adharNumber; }
    public void setAdharNumber(String adharNumber) { this.adharNumber = adharNumber; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
