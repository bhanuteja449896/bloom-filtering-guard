package com.bloomguard.exception;

public class FilterNotFoundException extends BloomFilterException {

    public FilterNotFoundException(String filterName) {
        super("Filter not found: " + filterName);
    }
}
