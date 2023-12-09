package com.rst.tableservice.usecase.port;


import com.rst.tableservice.core.model.Tables;

import java.util.List;
import java.util.Optional;

public interface TableDatasourcePort {

     Optional<Tables> getTableById(Long tableId);

     List<Tables> getAll();
}
