package com.bloomguard.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CheckRequest {

    @NotBlank(message = "Filter name is required")
    @Size(max = 100, message = "Filter name must not exceed 100 characters")
    private String filterName;

    @NotBlank(message = "Item is required")
    @Size(max = 1000, message = "Item must not exceed 1000 characters")
    private String item;

    public CheckRequest() {}

    public CheckRequest(String filterName, String item) {
        this.filterName = filterName;
        this.item = item;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }
}
