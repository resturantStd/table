package com.rst.tableservice.usecase.processor;

import com.rst.tableservice.core.exception.TableNotFoundException;
import com.rst.tableservice.core.model.TableCondition;
import com.rst.tableservice.core.model.TableStatusType;
import com.rst.tableservice.usecase.port.ReserveDatasourcePort;
import com.rst.tableservice.usecase.port.SanderPort;
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
    private final ReserveDatasourcePort reserveDatasourcePort;
    private final SanderPort sanderPort;


    /*
     * Working Logic:
     *
     * 1. Pre-reservation stage:
     *    - 30 minutes prior to the reservation time, the system updates the status of the table to OCCUPIED.
     *    - A message is then dispatched to the Message Queue (MQ) to notify relevant parties of this status change.
     *    - Despite the status change, the 'occupied' flag remains false. This indicates that while the table is reserved, it is not yet physically occupied.
     *    - At this stage, the table remains linked to the reservation.
     *
     * 2. Post-reservation stage:
     *    - 30 minutes after the reservation time, the system performs two actions:
     *        a. The table is disassociated from the reservation.
     *        b. The status of the table is updated to AVAILABLE.
     *    - Similar to the pre-reservation stage, the 'occupied' flag remains false even after these changes. This indicates that the table, while now available for new reservations, is not currently occupied.
     */


    @Scheduled(fixedDelay = 1000)
    public void execute() {
        log.info("process check reserved table is started {}", new Date());
        reserveDatasourcePort.getAllReservedTable().forEach(reservedTable -> {
            val reservedTime = reservedTable.reservedTimes();
            val reservedSize = reservedTime.size();
            reservedTime.forEach(time -> {
                val triggerTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(time), UTC);
                Long tableId = reservedTable.tableId();
                LocalDateTime now = LocalDateTime.now(UTC);

                log.info("table {} is reserved at {}", tableId, triggerTime);
                if (triggerTime.isAfter(now) && triggerTime.isBefore(now.plusMinutes(30))) {
                    tableConditionPort.updateStatus(TableStatusType.OCCUPIED, tableId);
                    sanderPort.send("table.reserve", tableId, "table is occupied");
                    log.info("table {} is occupied", reservedTable);
                } else if (triggerTime.isBefore(now) && triggerTime.plusMinutes(30).isBefore(now)) {
                    var tableCondition = getTableCondition(tableId);
                    removeReservation(tableCondition, triggerTime, reservedSize == 1);
                    log.info("table {} was unreserved", reservedTable);
                }
            });
        });
    }

    private TableCondition getTableCondition(Long tableId) {
        return tableConditionPort.getTableConditionByTableId(tableId)
                .onErrorResume(e -> {
                    throw new TableNotFoundException(tableId);
                })
                .block();
    }

    private void removeReservation(TableCondition tableCondition, LocalDateTime reservationTime, boolean isLastReservation) {
        long tableId = tableCondition.getTableId();
        tableCondition.setStatus(isLastReservation
                ? TableStatusType.AVAILABLE
                : TableStatusType.RESERVED);

        tableCondition.setOccupied(false);
        reserveDatasourcePort.deleteReservedTime(tableId, reservationTime);
        tableConditionPort.save(tableCondition);
        sanderPort.send("table.reserve", tableId, "table is unreserved");
    }
}
