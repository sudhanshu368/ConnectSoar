package com.connectsoar.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class CreateProjectRequest {

    @NotBlank(message = "Project name is required")
    private String name;

    private String description;

    @JsonProperty("start_date")
    private LocalDate startDate;

    @NotNull(message = "Project deadline is required")
    private LocalDate deadline;

    public CreateProjectRequest() {
    }

    public CreateProjectRequest(String name, String description, LocalDate startDate, LocalDate deadline) {
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.deadline = deadline;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
}
