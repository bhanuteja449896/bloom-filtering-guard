package com.bloomguard.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public class BatchAddRequest {

    @NotBlank(message = "Filter name is required")
    @Size(max = 100, message = "Filter name must not exceed 100 characters")
    private String filterName;

    @NotEmpty(message = "Items list cannot be empty")
    @Size(max = 1000, message = "Batch size must not exceed 1000 items")
    private List<String> items;

    public BatchAddRequest() {}

    public BatchAddRequest(String filterName, List<String> items) {
        this.filterName = filterName;
        this.items = items;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }
}
