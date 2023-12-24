package com.rst.tableservice.usecase.processor;

import com.rst.tableservice.core.model.ReservedTable;
import com.rst.tableservice.core.model.TableCondition;
import com.rst.tableservice.core.model.TableStatusType;
import com.rst.tableservice.usecase.port.ReserveDatasourcePort;
import com.rst.tableservice.usecase.port.SanderPort;
import com.rst.tableservice.usecase.port.TableConditionDatasourcePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.time.ZoneOffset.UTC;
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
    @DisplayName("should reserve table when reservation time is less than 30 minutes")
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
    @DisplayName("should unreserved table when next reservation time absent and reservation time expired by 30 minutes")
    void shouldUnreserveTableWhenNextReservationTimeAbsentAndReservationTimeExpiredBy30Minutes() {
        // Given
        Long tableId = 1L;
        Set<Long> reservationTimes = Set.of(LocalDateTime.now(UTC).minusMinutes(35).toEpochSecond(UTC));
        TableCondition tableCondition = TableCondition.builder()
                .tableId(tableId)
                .occupied(false)
                .status(TableStatusType.OCCUPIED)
                .build();

        TableCondition expectedTableCondition = TableCondition.builder()
                .tableId(tableId)
                .occupied(false)
                .status(TableStatusType.AVAILABLE)
                .build();

        // When
        when(reserveDatasourcePort.getAllReservedTable()).thenReturn(List.of(new ReservedTable(tableId, reservationTimes)));
        when(tableConditionPort.getTableConditionByTableId(tableId)).thenReturn(Optional.of(tableCondition));
        reserveTableProcessor.execute();

        // Then
        verify(reserveDatasourcePort, times(1)).deleteReservedTime(eq(tableId), any(LocalDateTime.class));
        verify(tableConditionPort, times(1)).save(expectedTableCondition);
        verify(sanderPort, times(1)).send("table.reserve", tableId, "table is unreserved");
    }
    @Test
    @DisplayName("Do not change table status when reservation time is more than 30 minutes")
    void shouldNotChangeTableStatusWhenReservationTimeIsMoreThan30Minutes() {
        //Given
        Set<Long> reservationTimes = Set.of(LocalDateTime.now(UTC).plusMinutes(35).toEpochSecond(UTC));
        Long table1 = 1L;
        ReservedTable reservedTable = new ReservedTable(table1, reservationTimes);

        //When
        when(reserveDatasourcePort.getAllReservedTable()).thenReturn(List.of(reservedTable));
        reserveTableProcessor.execute();

        //Then
        verify(reserveDatasourcePort, never()).deleteReservedTime(eq(table1), any(LocalDateTime.class));
        verify(tableConditionPort, never()).updateStatus(any(TableStatusType.class), eq(table1));
    }
}