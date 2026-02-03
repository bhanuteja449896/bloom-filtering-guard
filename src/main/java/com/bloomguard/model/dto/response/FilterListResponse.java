package com.bloomguard.model.dto.response;

import java.util.List;

public class FilterListResponse {

    private List<FilterInfo> filters;
    private int totalCount;

    public FilterListResponse() {}

    public FilterListResponse(List<FilterInfo> filters) {
        this.filters = filters;
        this.totalCount = filters != null ? filters.size() : 0;
    }

    public List<FilterInfo> getFilters() {
        return filters;
    }

    public void setFilters(List<FilterInfo> filters) {
        this.filters = filters;
        this.totalCount = filters != null ? filters.size() : 0;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public static class FilterInfo {
        private String filterName;
        private long expectedInsertions;
        private double falsePositiveRate;
        private boolean rotatable;
        private boolean active;

        public FilterInfo() {}

        public FilterInfo(String filterName, long expectedInsertions, double falsePositiveRate, 
                         boolean rotatable, boolean active) {
            this.filterName = filterName;
            this.expectedInsertions = expectedInsertions;
            this.falsePositiveRate = falsePositiveRate;
            this.rotatable = rotatable;
            this.active = active;
        }

        public String getFilterName() {
            return filterName;
        }

        public void setFilterName(String filterName) {
            this.filterName = filterName;
        }

        public long getExpectedInsertions() {
            return expectedInsertions;
        }

        public void setExpectedInsertions(long expectedInsertions) {
            this.expectedInsertions = expectedInsertions;
        }

        public double getFalsePositiveRate() {
            return falsePositiveRate;
        }

        public void setFalsePositiveRate(double falsePositiveRate) {
            this.falsePositiveRate = falsePositiveRate;
        }

        public boolean isRotatable() {
            return rotatable;
        }

        public void setRotatable(boolean rotatable) {
            this.rotatable = rotatable;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }
}
