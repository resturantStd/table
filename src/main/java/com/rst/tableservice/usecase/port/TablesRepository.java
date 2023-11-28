package com.rst.tableservice.usecase.port;



import com.rst.tableservice.core.model.TableStatusType;
import com.rst.tableservice.core.model.Tables;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TablesRepository {

     Optional<Tables> getTable(Long tableId);

     boolean updateStatus(TableStatusType statusType);

     boolean reserve(Long tableId, LocalDateTime from, LocalDateTime to);

     List<Tables> getAll();
}
