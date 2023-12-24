package com.rst.tableservice.usecase;

import com.rst.tableservice.core.exception.TableNotAvailableException;
import com.rst.tableservice.core.exception.TimeNotAvailableException;
import com.rst.tableservice.core.model.TableCondition;
import com.rst.tableservice.core.model.TableStatusType;
import com.rst.tableservice.usecase.port.ReserveDatasourcePort;
import com.rst.tableservice.usecase.port.TableConditionDatasourcePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static java.time.ZoneOffset.UTC;

@Slf4j
@Service
@RequiredArgsConstructor
public class OccupiedTableUseCase {

    private final TableConditionDatasourcePort tableConditionDatasourcePort;
    private final ReserveDatasourcePort reserveDatasourcePort;

    public void execute(long tableId) {
        log.info("Try to occupy table {}", tableId);
        tableConditionDatasourcePort.getTableConditionByTableId(tableId)
                .ifPresentOrElse(tableCondition -> {
                            validateIsTimeAvailable(tableCondition);
                            tableCondition.setStatus(TableStatusType.OCCUPIED);
                            tableCondition.setOccupied(true);
                            tableConditionDatasourcePort.save(tableCondition);
                            log.info("Table {} is occupied, condition was exist", tableId);
                        }, () -> {
                            tableConditionDatasourcePort.save(
                                    TableCondition.builder()
                                            .tableId(tableId)
                                            .occupied(true)
                                            .status(TableStatusType.OCCUPIED)
                                            .build()
                            );
                            log.info("Table {} is occupied, condition was created", tableId);
                        }
                );
    }

    private void validateIsTimeAvailable(TableCondition tableCondition) {
        if (tableCondition.isOccupied() || tableCondition.getStatus() == TableStatusType.OCCUPIED) {
            throw new TableNotAvailableException(tableCondition.getTableId());
        }

        TableStatusType status = tableCondition.getStatus();
        if (status == TableStatusType.RESERVED) {
            reserveDatasourcePort.getReservedTime(tableCondition.getTableId())
                    .stream()
                    .map(time -> LocalDateTime.ofEpochSecond(time, 0, UTC))
                    .filter(triggerTime -> triggerTime.isAfter(LocalDateTime.now(UTC).minusMinutes(120)) && triggerTime.isBefore(LocalDateTime.now(UTC).plusMinutes(120)))
                    .findFirst()
                    .ifPresent(triggerTime -> {
                        if (LocalDateTime.now(UTC).isAfter(triggerTime.plusMinutes(30))) {
                            // If the current time is more than 30 minutes after the reservation time,
                            // the table is no longer considered reserved.
                            return;
                        }
                        throw new TimeNotAvailableException(tableCondition.getTableId());
                    });
        }
    }
}
