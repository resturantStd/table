package com.rst.tableservice.usecase.port;



import com.rst.tableservice.core.model.TableStatusType;
import com.rst.tableservice.core.model.Tables;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TablesRepository {

     Optional<Tables> getTable(Long tableId);

     void updateStatus(TableStatusType statusType, Long tableId);

     void reserve(Long tableId, LocalDateTime from);

     List<Tables> getAll();
}
