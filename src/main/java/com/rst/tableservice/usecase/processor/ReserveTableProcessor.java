package com.rst.tableservice.usecase.processor;

import com.rst.tableservice.core.model.TableStatusType;
import com.rst.tableservice.usecase.port.TableConditionDatasourcePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

import static java.time.ZoneOffset.UTC;


/*
 * Documentation
 *
 * Reserve table if time is less than 30 minutes
 *  and remove the reservation after 30 minutes if the table is not occupied.
 *
 * Key in REDIS "table.reserve" is a hash table with 'key' as table id
 *  and 'value' as SET of times in seconds when the table is reserved.
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class ReserveTableProcessor {
    private final TableConditionDatasourcePort tableConditionPort;

    @Scheduled(fixedDelay = 1000)
    public void execute() {
        log.info("process check reserved table is started {}", new Date());
        tableConditionPort.getAllReservedTable().forEach(reservedTable -> {
            val reservedTime =  reservedTable.reservedTimes();
            reservedTime.forEach(time -> {
                val triggerTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(time), UTC);
                if (triggerTime.isBefore(LocalDateTime.now().plusMinutes(30))) {
                    tableConditionPort.deleteReservedTime(reservedTable.tableId(), triggerTime);
                    //TODO sand message to MQ to reserve table if the table is not occupied
                    log.info("table {} was unreserved", reservedTable);
                } else if (triggerTime.isAfter(LocalDateTime.now().minusMinutes(30))) {
                    tableConditionPort.updateStatus(TableStatusType.OCCUPIED, reservedTable.tableId());
                    //TODO sand message to MQ to reserve table if the table is not occupied
                    log.info("table {} is occupied", reservedTable);
                }
            });
        });
    }
}
