package com.rst.tableservice.usecase;



import com.rst.tableservice.core.model.Tables;
import com.rst.tableservice.usecase.port.TablesRepository;

import java.util.List;

public class GetTablesUseCase {

    private final TablesRepository tablesRepository;

    public GetTablesUseCase(TablesRepository tablesRepository) {
        this.tablesRepository = tablesRepository;
    }

    public Tables getById(Long id) {
        return tablesRepository.getTable(id).orElseThrow();
    }

    public List<Tables> getAll() {
        return tablesRepository.getAll();
    }
}
