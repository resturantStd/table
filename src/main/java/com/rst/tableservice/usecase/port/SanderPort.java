package com.rst.tableservice.usecase.port;

public interface SanderPort {
    void send(String message, Long aLong, String tableIsUnreserved);
}
