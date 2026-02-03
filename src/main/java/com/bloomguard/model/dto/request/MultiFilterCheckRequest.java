package com.bloomguard.model.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;

public class MultiFilterCheckRequest {

    @NotEmpty(message = "Filter names list cannot be empty")
    @Size(max = 10, message = "Cannot check more than 10 filters at once")
    private List<@NotBlank @Size(max = 100) String> filterNames;

    @NotBlank(message = "Item is required")
    @Size(max = 1000, message = "Item must not exceed 1000 characters")
    private String item;

    public MultiFilterCheckRequest() {}

    public MultiFilterCheckRequest(List<String> filterNames, String item) {
        this.filterNames = filterNames;
        this.item = item;
    }

    public List<String> getFilterNames() {
        return filterNames;
    }

    public void setFilterNames(List<String> filterNames) {
        this.filterNames = filterNames;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }
}
