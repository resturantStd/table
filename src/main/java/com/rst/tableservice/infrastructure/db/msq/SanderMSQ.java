package com.rst.tableservice.infrastructure.db.msq;

import com.rst.tableservice.usecase.port.SanderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SanderMSQ implements SanderPort {
    @Override
    public void send(String message, Long aLong, String tableIsUnreserved) {
        log.info("SanderMSQ: " + message);
    }
}
