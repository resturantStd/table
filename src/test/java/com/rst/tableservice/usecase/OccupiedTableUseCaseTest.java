package com.rst.tableservice.usecase;


import com.rst.tableservice.core.exception.TimeNotAvailableException;
import com.rst.tableservice.core.model.TableCondition;
import com.rst.tableservice.core.model.TableStatusType;
import com.rst.tableservice.usecase.port.ReserveDatasourcePort;
import com.rst.tableservice.usecase.port.TableConditionDatasourcePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/*
* Тут не учитывается автоматическое освобождение столика через 30 минут после резерва
* реализация ReserveTableProcessor
*
* */

class OccupiedTableUseCaseTest {

    @Mock
    private TableConditionDatasourcePort tableConditionDatasourcePort;
    @Mock
    private ReserveDatasourcePort reserveDatasourcePort;

    private OccupiedTableUseCase occupiedTableUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        occupiedTableUseCase = new OccupiedTableUseCase(tableConditionDatasourcePort, reserveDatasourcePort);
    }

    @Test
    @DisplayName("Should execute when table is not occupied and not reserved")
    void shouldExecuteWhenTableIsNotOccupiedAndNotReserved() {
        //Given
        long tableId = 1L;
        TableCondition tableCondition = TableCondition.builder()
                .tableId(tableId)
                .occupied(false)
                .status(TableStatusType.AVAILABLE)
                .build();

        //When
        when(tableConditionDatasourcePort.getTableConditionByTableId(tableId)).thenReturn(Optional.of(tableCondition));
        occupiedTableUseCase.execute(tableId);

        //Then
        verify(tableConditionDatasourcePort, times(1)).updateStatus(TableStatusType.OCCUPIED, tableId);
    }

    @Test
    @DisplayName("Should execute when table is not present")
    void shouldExecuteWhenTableIsNotPresent() {
        //Given
        long tableId = 1L;

        //When
        when(tableConditionDatasourcePort.getTableConditionByTableId(tableId)).thenReturn(Optional.empty());
        occupiedTableUseCase.execute(tableId);

        //Then
        verify(tableConditionDatasourcePort, times(1)).save(any(TableCondition.class));
    }

    @Test
    @DisplayName("Should execute when table is not occupied and reserved but reservation time is not within 120 minutes")
    void shouldExecuteWhenTableIsNotOccupiedAndReservedButReservationTimeIsNotWithinTwoHours() {
        //Given
        long tableId = 1L;
        Set<Long> reservationTimes = Set.of(LocalDateTime.now(UTC).minusHours(3).toEpochSecond(UTC), LocalDateTime.now(UTC).plusHours(3).toEpochSecond(UTC));
        TableCondition tableCondition = TableCondition.builder()
                .tableId(tableId)
                .occupied(false)
                .status(TableStatusType.RESERVED)
                .build();

        //When
        when(tableConditionDatasourcePort.getTableConditionByTableId(tableId)).thenReturn(Optional.of(tableCondition));
        when(reserveDatasourcePort.getReservedTime(tableId)).thenReturn(reservationTimes);

        occupiedTableUseCase.execute(tableId);

        //Then
        verify(tableConditionDatasourcePort, times(1)).updateStatus(TableStatusType.OCCUPIED, tableId);
    }

    @Test
    @DisplayName("Should execute when table is not occupied and reserved but no reservation time")
    void shouldExecuteWhenTableIsNotOccupiedAndReservedButNoReservationTime() {
        //Given
        long tableId = 1L;
        Set<Long> reservationTimes = Set.of();
        TableCondition tableCondition = TableCondition.builder()
                .tableId(tableId)
                .occupied(false)
                .status(TableStatusType.RESERVED)
                .build();

        //When
        when(tableConditionDatasourcePort.getTableConditionByTableId(tableId)).thenReturn(Optional.of(tableCondition));
        when(reserveDatasourcePort.getReservedTime(tableId)).thenReturn(reservationTimes);

        occupiedTableUseCase.execute(tableId);

        //Then
        verify(tableConditionDatasourcePort, times(1)).updateStatus(TableStatusType.OCCUPIED, tableId);
    }

    @Test
    @DisplayName("Should execute when table is occupied")
    void shouldThrowExceptionWhenTableIsOccupied() {
        //Given
        long tableId = 1L;
        TableCondition tableCondition = TableCondition.builder()
                .tableId(tableId)
                .occupied(true)
                .status(TableStatusType.OCCUPIED)
                .build();

        //When
        when(tableConditionDatasourcePort.getTableConditionByTableId(tableId)).thenReturn(Optional.of(tableCondition));

        assertThrows(RuntimeException.class, () -> occupiedTableUseCase.execute(tableId));
    }

    @Test
    @DisplayName("Should throw exception when table is reserved and reservation times are within 2 hours")
    void shouldThrowExceptionWhenTableIsReservedAndReservationTimesAreWithinTwoHours() {
        //Given
        long tableId = 1L;
        Set<Long> reservationTimes = Set.of(LocalDateTime.now(UTC).minusMinutes(120).toEpochSecond(UTC), LocalDateTime.now(UTC).plusMinutes(120).toEpochSecond(UTC));
        TableCondition tableCondition = TableCondition.builder()
                .tableId(tableId)
                .occupied(false)
                .status(TableStatusType.RESERVED)
                .build();

        //When
        when(tableConditionDatasourcePort.getTableConditionByTableId(tableId)).thenReturn(Optional.of(tableCondition));
        when(reserveDatasourcePort.getReservedTime(tableId)).thenReturn(reservationTimes);

        //Then
        assertThrows(TimeNotAvailableException.class, () -> occupiedTableUseCase.execute(tableId));
    }

    @Test
    @DisplayName("Should throw exception when table is reserved and reservation time is exactly half an hour before")
    void shouldThrowExceptionWhenTableIsReservedAndReservationTimeIsExactlyHalfAnHourBefore() {
        //Given
        long tableId = 1L;
        Set<Long> reservationTimes = Set.of(LocalDateTime.now(UTC).plusMinutes(30).toEpochSecond(UTC));
        TableCondition tableCondition = TableCondition.builder()
                .tableId(tableId)
                .occupied(false)
                .status(TableStatusType.RESERVED)
                .build();

        //When
        when(tableConditionDatasourcePort.getTableConditionByTableId(tableId)).thenReturn(Optional.of(tableCondition));
        when(reserveDatasourcePort.getReservedTime(tableId)).thenReturn(reservationTimes);

        //Then
        assertThrows(TimeNotAvailableException.class, () -> occupiedTableUseCase.execute(tableId));
    }

    @Test
    @DisplayName("Should throw exception when table is reserved and reservation time is exactly 120 minutes after")
    void shouldThrowExceptionWhenTableIsReservedAndReservationTimeIsExactlyTwoHoursAhead() {
        //Given
        long tableId = 1L;
        Set<Long> reservationTimes = Set.of(LocalDateTime.now(UTC).plusHours(2).toEpochSecond(UTC));
        TableCondition tableCondition = TableCondition.builder()
                .tableId(tableId)
                .occupied(false)
                .status(TableStatusType.RESERVED)
                .build();

        //When
        when(tableConditionDatasourcePort.getTableConditionByTableId(tableId)).thenReturn(Optional.of(tableCondition));
        when(reserveDatasourcePort.getReservedTime(tableId)).thenReturn(reservationTimes);

        //Then
        assertThrows(RuntimeException.class, () -> occupiedTableUseCase.execute(tableId));
    }
}