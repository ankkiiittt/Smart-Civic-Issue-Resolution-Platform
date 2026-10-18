package com.smartcivic.dto;

public class OfficerSummaryResponse {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String departmentName;
    private boolean available;

    public OfficerSummaryResponse() {}

    public OfficerSummaryResponse(Long id, String username, String firstName, String lastName, String departmentName, boolean available) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.departmentName = departmentName;
        this.available = available;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
