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
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static java.time.ZoneOffset.UTC;

@Slf4j
@Service
@RequiredArgsConstructor
public class OccupiedTableUseCase {

    private final TableConditionDatasourcePort tableConditionDatasourcePort;
    private final ReserveDatasourcePort reserveDatasourcePort;


    public Mono<Void> execute(long tableId) {
        log.info("Try to occupy table {}", tableId);
        return tableConditionDatasourcePort.getTableConditionByTableId(tableId)
                .defaultIfEmpty(TableCondition.builder()
                        .tableId(tableId)
                        .occupied(false)
                        .status(TableStatusType.AVAILABLE)
                        .build())
                .flatMap(tableCondition -> {
                    try {
                        validateIsTimeAvailable(tableCondition);
                        tableCondition.setStatus(TableStatusType.OCCUPIED);
                        tableCondition.setOccupied(true);
                        return Mono.just(tableCondition);
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                })
                .doOnError(e -> log.error("Table {} occupation failed due to error: ", tableId, e))
                .flatMap(tableCondition -> tableConditionDatasourcePort.save(tableCondition)
                        .doOnSuccess(v -> log.info("Table {} is occupied, condition was saved", tableId))
                        .then())
                .then();
    }


    private void validateIsTimeAvailable(TableCondition tableCondition) {
        LocalDateTime now = LocalDateTime.now(UTC);

        if (tableCondition.isOccupied() || tableCondition.getStatus() == TableStatusType.OCCUPIED) {
            throw new TableNotAvailableException(tableCondition.getTableId());
        }

        TableStatusType status = tableCondition.getStatus();
        if (status == TableStatusType.RESERVED) {
            reserveDatasourcePort.getReservedTime(tableCondition.getTableId())
                    .stream()
                    .map(time -> LocalDateTime.ofEpochSecond(time, 0, UTC))
                    .filter(triggerTime -> triggerTime.isAfter(now.minusHours(2)) && triggerTime.isBefore(now.plusHours(2)))
                    .sorted()
                    .findFirst().ifPresent(triggerTime -> {
                                if (now.minusHours(1).minusMinutes(29).isAfter(triggerTime)) {
                                    return;
                                }
                                throw new TimeNotAvailableException(tableCondition.getTableId());
                            }
                    );
        }
    }
}
