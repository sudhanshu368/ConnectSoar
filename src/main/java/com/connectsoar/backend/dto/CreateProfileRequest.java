package com.connectsoar.backend.dto;

public class CreateProfileRequest {
    private String fullName;
    private String phoneNumber;
    private String department;
    private String designation;
    private String address;

    public CreateProfileRequest() {}
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
