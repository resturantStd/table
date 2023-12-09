package com.rst.tableservice.usecase.port;



import com.rst.tableservice.core.model.TableStatusType;
import com.rst.tableservice.core.model.Tables;

import java.util.List;
import java.util.Optional;

public interface TablesPort {

     Optional<Tables> getTableById(Long tableId);

     void updateStatus(TableStatusType statusType, Long tableId);

     List<Tables> getAll();
}
