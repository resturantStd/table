package com.rst.tableservice.core.exception;

public class TableNotAvailableException extends RuntimeException {
    public TableNotAvailableException(Long id) {
        super("Table with id " + id + " is not available");
    }
}
