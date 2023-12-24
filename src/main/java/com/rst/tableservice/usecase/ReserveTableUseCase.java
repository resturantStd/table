package com.rst.tableservice.usecase;

import com.rst.tableservice.core.exception.TimeNotAvailableException;
import com.rst.tableservice.usecase.port.ReserveDatasourcePort;
import com.rst.tableservice.usecase.port.TableDatasourcePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

import static java.time.ZoneOffset.UTC;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReserveTableUseCase {
    private final TableDatasourcePort tableDatasourcePort;
    private final ReserveDatasourcePort reserveDatasourcePort;



    public void execute(Long tableId, LocalDateTime from) {
        log.info("Reserve table {} from {}", tableId, from);
        tableDatasourcePort.getTableById(tableId)
                .ifPresentOrElse(table -> {
                    Set<Long> reservedTime = reserveDatasourcePort.getReservedTime(tableId);
                    validateIsTimeAvailable(from, reservedTime, tableId);
                    reserveDatasourcePort.setTableReservationTime(tableId, from);
                    log.info("Table {} is reserved from {}", tableId, from);
                }, () -> {
                    throw new RuntimeException("Table not found");
                });
    }


    /*
   check if the time is available time is 120 minutes before from time and 120 minutes after from time
   if the time is not available then throw exception
   if the time is not available then throw exception
   */
    private void validateIsTimeAvailable(LocalDateTime from, Set<Long> timeSet, long tableId) {
        timeSet.stream()
                .map(time -> LocalDateTime.ofEpochSecond(time, 0, UTC))
                .filter(triggerTime -> triggerTime.isAfter(from.minusMinutes(120)) && triggerTime.isBefore(from.plusMinutes(120)))
                .findFirst()
                .ifPresent(triggerTime -> {
                    throw new TimeNotAvailableException(tableId);
                });
    }
}
