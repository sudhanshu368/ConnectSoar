package com.connectsoar.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateEmployeeRequest {

    private String name;
    private String department;
    private String designation;
    private String phone;

    @JsonProperty("image_url")
    private String imageUrl;

    public UpdateEmployeeRequest() {
    }

    public UpdateEmployeeRequest(String name, String department, String designation, String phone, String imageUrl) {
        this.name = name;
        this.department = department;
        this.designation = designation;
        this.phone = phone;
        this.imageUrl = imageUrl;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String department;
        private String designation;
        private String phone;
        private String imageUrl;

        public Builder name(String name) { this.name = name; return this; }
        public Builder department(String department) { this.department = department; return this; }
        public Builder designation(String designation) { this.designation = designation; return this; }
        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder imageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }

        public UpdateEmployeeRequest build() {
            return new UpdateEmployeeRequest(name, department, designation, phone, imageUrl);
        }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
