package com.rst.tableservice.core.exception;

public class TimeNotAvailableException extends RuntimeException {
    public TimeNotAvailableException(Long id) {
        super("Table with id " + id + " is reserved at this time");
    }
}
