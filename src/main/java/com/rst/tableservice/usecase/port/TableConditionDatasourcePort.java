package com.rst.tableservice.usecase.port;

import com.rst.tableservice.core.model.ReservedTable;
import com.rst.tableservice.core.model.TableCondition;
import com.rst.tableservice.core.model.TableStatusType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TableConditionDatasourcePort {
    void updateStatus(TableStatusType statusType, long tableId);

    Optional<TableCondition> getTableConditionByTableId(long tableId);

    void setTableReservationTime(long tableId, LocalDateTime time);


    Set<Long> getReservedTime(long tableId);

    void deleteReservedTime(long tableId, LocalDateTime time);

    List<ReservedTable> getAllReservedTable();
}
