package com.rst.tableservice.usecase.port;

import com.rst.tableservice.core.model.ReservedTable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface ReserveDatasourcePort {
    void setTableReservationTime(long tableId, LocalDateTime time);

    Set<Long> getReservedTime(long tableId);

    void deleteReservedTime(long tableId, LocalDateTime time);

    List<ReservedTable> getAllReservedTable();
}
