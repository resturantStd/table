package com.rst.tableservice.infrastructure.datasource;

import com.rst.tableservice.core.exception.InvalidStatusException;
import com.rst.tableservice.core.exception.TableNotFoundException;
import com.rst.tableservice.core.model.TableStatusType;
import com.rst.tableservice.core.model.Tables;
import com.rst.tableservice.infrastructure.db.jdbc.TableRepository;
import com.rst.tableservice.infrastructure.db.redis.TableConditionRepository;
import com.rst.tableservice.usecase.port.TablesPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TableDatasource implements TablesPort {
    private final TableRepository tableRepository;
    private final TableConditionRepository tableConditionRepository;

    public TableDatasource(TableRepository tableRepository, TableConditionRepository tableConditionRepository) {
        this.tableRepository = tableRepository;
        this.tableConditionRepository = tableConditionRepository;
    }

    @Override
    public List<Tables> getAll() {
        return tableRepository.findAll();
    }

    @Override
    public Optional<Tables> getTableById(Long tableId) {
        return tableRepository.findById(tableId);
    }

    @Override
    public void updateStatus(TableStatusType statusType, Long tableId) {
        if (statusType == TableStatusType.RESERVED) {
            throw new InvalidStatusException("Invalid status");
        }

        tableConditionRepository.getTableConditionByTableId(tableId)
                .ifPresentOrElse(tableCondition -> {
                    tableCondition.setOccupied(statusType == TableStatusType.OCCUPIED);
                    tableConditionRepository.save(tableCondition);
                }, () -> {
                    throw new TableNotFoundException(tableId);
                });
    }
}
