package com.rst.tableservice.usecase;

import com.rst.tableservice.core.model.TableCondition;
import com.rst.tableservice.core.model.TableStatusType;
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

    public void execute(long tableId) {
        tableConditionDatasourcePort.getTableConditionByTableId(tableId)
                .ifPresentOrElse(tableCondition -> {
                            validateIsTimeAvailable(tableCondition);
                            tableConditionDatasourcePort.updateStatus(TableStatusType.OCCUPIED, tableId);

                        }, () ->
                                tableConditionDatasourcePort.save(
                                        TableCondition.builder()
                                                .tableId(tableId)
                                                .occupied(true)
                                                .status(TableStatusType.OCCUPIED)
                                                .build()
                                )
                );

    }

    private void validateIsTimeAvailable(TableCondition tableCondition) {
        if (tableCondition.isOccupied()) {
            throw new RuntimeException("Table is occupied");
        }

        TableStatusType status = tableCondition.getStatus();
        if (status == TableStatusType.RESERVED) {
            tableConditionDatasourcePort.getReservedTime(tableCondition.getTableId())
                    .stream()
                    .map(time -> LocalDateTime.ofEpochSecond(time, 0, UTC))
                    .filter(triggerTime -> triggerTime.isAfter(LocalDateTime.now(UTC).minusMinutes(120)) && triggerTime.isBefore(LocalDateTime.now(UTC).plusMinutes(120)))
                    .findFirst()
                    .ifPresent(triggerTime -> {
                        throw new RuntimeException("Table is reserved");
                    });
        }

    }
}
