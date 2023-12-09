package com.rst.tableservice.usecase;



import com.rst.tableservice.core.model.Tables;
import com.rst.tableservice.usecase.port.TablesPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetTablesUseCase {

    private final TablesPort tablesPort;

    public GetTablesUseCase(TablesPort tablesPort) {
        this.tablesPort = tablesPort;
    }

    public Tables getById(Long id) {
        return tablesPort.getTableById(id).orElseThrow();
    }

    public List<Tables> getAll() {
        return tablesPort.getAll();
    }
}
