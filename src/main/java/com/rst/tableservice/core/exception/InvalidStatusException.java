package com.rst.tableservice.core.exception;

public class InvalidStatusException extends RuntimeException{

    public InvalidStatusException(String status) {
        super("Invalid status: " + status);
    }
}
