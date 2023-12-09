package com.rst.tableservice.usecase;



import com.rst.tableservice.core.model.Tables;
import com.rst.tableservice.usecase.port.TableDatasourcePort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetTablesUseCase {

    private final TableDatasourcePort tableDatasourcePort;

    public GetTablesUseCase(TableDatasourcePort tableDatasourcePort) {
        this.tableDatasourcePort = tableDatasourcePort;
    }

    public Tables getById(Long id) {
        return tableDatasourcePort.getTableById(id).orElseThrow();
    }

    public List<Tables> getAll() {
        return tableDatasourcePort.getAll();
    }
}
