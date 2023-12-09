package com.rst.tableservice.infrastructure.datasource;

import com.rst.tableservice.core.model.Tables;
import com.rst.tableservice.infrastructure.db.jdbc.TableRepository;
import com.rst.tableservice.usecase.port.TableDatasourcePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TableDatasource implements TableDatasourcePort {
    private final TableRepository tableRepository;

    @Override
    public List<Tables> getAll() {
        return tableRepository.findAll();
    }

    @Override
    public Optional<Tables> getTableById(Long tableId) {
        return tableRepository.findById(tableId);
    }

}
