package com.rst.tableservice.infrastructure.datasource;

import com.rst.tableservice.core.exception.InvalidStatusException;
import com.rst.tableservice.core.exception.TableNotFoundException;
import com.rst.tableservice.core.model.TableCondition;
import com.rst.tableservice.core.model.TableStatusType;
import com.rst.tableservice.infrastructure.db.redis.TableConditionRepository;
import com.rst.tableservice.usecase.port.TableConditionDatasourcePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor

public class TableConditionDatasource implements TableConditionDatasourcePort {

    private final TableConditionRepository tableConditionRepository;


    @Override
    public void updateStatus(TableStatusType statusType, long tableId) {
        if (statusType == TableStatusType.RESERVED) {
            throw new InvalidStatusException("Invalid status");
        }

        tableConditionRepository.getTableConditionByTableId(tableId)
                .subscribe(tableCondition -> {
                    tableCondition.setStatus(statusType);
                    tableConditionRepository.save(tableCondition);
                }, error -> {
                    throw new RuntimeException(new TableNotFoundException(tableId));
                });
    }

    @Override
    public Mono<TableCondition> getTableConditionByTableId(long tableId) {
        return tableConditionRepository.getTableConditionByTableId(tableId);
    }

    public Mono<TableCondition> save(TableCondition tableCondition) {
        return tableConditionRepository.save(tableCondition);
    }
}
