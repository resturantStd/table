package com.rst.tableservice.usecase.port;

import com.rst.tableservice.core.model.ReservedTable;
import com.rst.tableservice.core.model.TableCondition;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TableConditionPort {
    void reserve(Long tableId, LocalDateTime from);
    Set<Long> getReservedTime(Long tableId);
    void deleteReservedTime(Long tableId, LocalDateTime time);

    List<ReservedTable> getAllReservedTable();
}
