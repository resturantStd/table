package com.rst.tableservice.infrastructure.processor.unit;

import com.rst.tableservice.core.model.ReservedTable;
import com.rst.tableservice.core.model.TableStatusType;
import com.rst.tableservice.usecase.port.ReserveDatasourcePort;
import com.rst.tableservice.usecase.port.SanderPort;
import com.rst.tableservice.usecase.port.TableConditionDatasourcePort;
import com.rst.tableservice.usecase.processor.ReserveTableProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static java.time.ZoneOffset.UTC;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


class ReserveTableProcessorTest {

    @Mock
    TableConditionDatasourcePort tableConditionPort;
    @Mock
    ReserveDatasourcePort reserveDatasourcePort;
    @Mock
    SanderPort sanderPort;

    @InjectMocks
    private ReserveTableProcessor reserveTableProcessor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("should reserve table when  reservation time is less than 30 minutes")
    void shouldReserveTableWhenParamsAllow() {
        // Given
        Long tableId = 1L;
        Set<Long> reservationTimes = Set.of(LocalDateTime.now(UTC).plusMinutes(29).toEpochSecond(UTC));
        when(reserveDatasourcePort.getAllReservedTable()).thenReturn(List.of(new ReservedTable(tableId, reservationTimes)));

        // When
        reserveTableProcessor.execute();

        // Then
        verify(tableConditionPort, times(1)).updateStatus(TableStatusType.OCCUPIED, tableId);
        verify(sanderPort, times(1)).send("table.reserve", tableId, "table is occupied");
    }

    @Test
    void shouldUnreserveTableWhenNextReservationTimeAbsentAndReservationTimeExpiredBy30Minutes() {
        // Given
        Long tableId = 1L;
        Set<Long> reservationTimes = Set.of(LocalDateTime.now(UTC).minusMinutes(30).toEpochSecond(UTC));
        when(reserveDatasourcePort.getAllReservedTable()).thenReturn(List.of(new ReservedTable(tableId, reservationTimes)));

        // When
        reserveTableProcessor.execute();

        // Then
        verify(reserveDatasourcePort, times(1)).deleteReservedTime(eq(tableId), any(LocalDateTime.class));
        verify(tableConditionPort, times(1)).updateStatus(TableStatusType.AVAILABLE, tableId);
        verify(sanderPort, times(1)).send("table.reserve", tableId, "table is unreserved");
    }


}