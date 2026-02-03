package com.bloomguard.model.dto.request;

import jakarta.validation.constraints.*;

public class CreateFilterRequest {

    @NotBlank(message = "Filter name is required")
    @Size(max = 100, message = "Filter name must not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Filter name can only contain alphanumeric characters, underscores, and hyphens")
    private String filterName;

    @NotNull(message = "Expected insertions is required")
    @Min(value = 1000, message = "Expected insertions must be at least 1000")
    @Max(value = 1_000_000_000L, message = "Expected insertions must not exceed 1 billion")
    private Long expectedInsertions;

    @NotNull(message = "False positive rate is required")
    @DecimalMin(value = "0.0001", message = "False positive rate must be at least 0.0001")
    @DecimalMax(value = "0.1", message = "False positive rate must not exceed 0.1")
    private Double falsePositiveRate;

    private boolean rotatable = false;

    @Min(value = 1, message = "Rotation days must be at least 1")
    @Max(value = 365, message = "Rotation days must not exceed 365")
    private Integer rotationDays;

    public CreateFilterRequest() {}

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public Long getExpectedInsertions() {
        return expectedInsertions;
    }

    public void setExpectedInsertions(Long expectedInsertions) {
        this.expectedInsertions = expectedInsertions;
    }

    public Double getFalsePositiveRate() {
        return falsePositiveRate;
    }

    public void setFalsePositiveRate(Double falsePositiveRate) {
        this.falsePositiveRate = falsePositiveRate;
    }

    public boolean isRotatable() {
        return rotatable;
    }

    public void setRotatable(boolean rotatable) {
        this.rotatable = rotatable;
    }

    public Integer getRotationDays() {
        return rotationDays;
    }

    public void setRotationDays(Integer rotationDays) {
        this.rotationDays = rotationDays;
    }
}
