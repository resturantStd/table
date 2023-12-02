package com.rst.tableservice.core.exception;

public class TableNotFoundException extends RuntimeException {
    public TableNotFoundException(Long id) {
        super("Table with id " + id + " not found");
    }
}
