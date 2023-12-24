package com.rst.tableservice.infrastructure.datasource;

import com.rst.tableservice.core.exception.InvalidStatusException;
import com.rst.tableservice.core.exception.TableNotFoundException;
import com.rst.tableservice.core.model.TableCondition;
import com.rst.tableservice.core.model.TableStatusType;
import com.rst.tableservice.infrastructure.db.redis.TableConditionRepository;
import com.rst.tableservice.usecase.port.TableConditionDatasourcePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor

public class TableConditionDatasource implements TableConditionDatasourcePort {

    private final TableConditionRepository tableConditionRepository;


    @Override
    public void updateStatus(TableStatusType statusType, long tableId) {
        if (statusType == TableStatusType.RESERVED) {
            throw new InvalidStatusException("Invalid status");
        }

        getTableConditionByTableId(tableId)
                .ifPresentOrElse(tableCondition -> {
                    tableCondition.setStatus(statusType);
                    tableConditionRepository.save(tableCondition);
                }, () -> {
                    throw new TableNotFoundException(tableId);
                });
    }

    @Override
    public Optional<TableCondition> getTableConditionByTableId(long tableId) {
        return tableConditionRepository.getTableConditionByTableId(tableId);
    }

    public void save(TableCondition tableCondition) {
        tableConditionRepository.save(tableCondition);
    }
}
