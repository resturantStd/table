package com.rst.tableservice.usecase;


import com.rst.tableservice.core.exception.TimeNotAvailableException;
import com.rst.tableservice.core.model.Tables;
import com.rst.tableservice.usecase.port.ReserveDatasourcePort;
import com.rst.tableservice.usecase.port.TableDatasourcePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ReserveTableUseCaseTest {
    private ReserveTableUseCase reserveTableUseCase;
    private TableDatasourcePort tableDatasourcePort;
    private ReserveDatasourcePort reserveDatasourcePort;

    @BeforeEach
    void setUp() {
        tableDatasourcePort = Mockito.mock(TableDatasourcePort.class);
        reserveDatasourcePort = Mockito.mock(ReserveDatasourcePort.class);
        reserveTableUseCase = new ReserveTableUseCase(tableDatasourcePort, reserveDatasourcePort);
    }

    @Test
    @DisplayName("Should throw exception when reservation time is within 120 minutes of another reservation")
    void shouldThrowExceptionWhenReservationTimeIsWithin120MinutesOfAnotherReservation() {
        //Given
        LocalDateTime reservationTime = LocalDateTime.now(UTC);
        Set<Long> reservedTimes = new HashSet<>();
        reservedTimes.add(reservationTime.plusMinutes(60).toEpochSecond(UTC));
        Tables table = new Tables(1L, 1);

        //When
        when(reserveDatasourcePort.getReservedTime(anyLong())).thenReturn(reservedTimes);
        when(tableDatasourcePort.getTableById(anyLong())).thenReturn(Optional.of(table));

        //Then
        assertThrows(TimeNotAvailableException.class, () -> reserveTableUseCase.execute(1L, reservationTime));
    }

    @Test
    @DisplayName("Should throw exception when table is not found")
    void shouldThrowExceptionWhenTableIsNotFound() {
        //Given
        LocalDateTime reservationTime = LocalDateTime.now(UTC);
        Set<Long> reservedTimes = new HashSet<>();
        reservedTimes.add(reservationTime.plusMinutes(60).toEpochSecond(UTC));
        Tables table = new Tables(1L, 1);

        //When
        when(reserveDatasourcePort.getReservedTime(anyLong())).thenReturn(reservedTimes);
        when(tableDatasourcePort.getTableById(anyLong())).thenReturn(Optional.empty());

        //Then
        assertThrows(RuntimeException.class, () -> reserveTableUseCase.execute(1L, reservationTime));
    }


    @Test
    @DisplayName("Should save reservation time")
    void shouldSaveReservationTimeIfAvailable() {
        //Given
        LocalDateTime reservationTime = LocalDateTime.now(UTC);
        Set<Long> reservedTimes = new HashSet<>();
        reservedTimes.add(reservationTime.plusMinutes(130).toEpochSecond(UTC));
        Tables table = new Tables(1L, 1);

        //When
        when(reserveDatasourcePort.getReservedTime(anyLong())).thenReturn(reservedTimes);
        when(tableDatasourcePort.getTableById(anyLong())).thenReturn(Optional.of(table));
        reserveTableUseCase.execute(1L, reservationTime);

        //Then
        verify(reserveDatasourcePort, times(1)).setTableReservationTime(anyLong(), any(LocalDateTime.class));
    }


    @Test
    @DisplayName("Should save reservation time when reservation time is within 120 minutes of another reservation")
    void shouldSaveReservationTimeWhenReservationTimeIsWithin120MinutesOfAnotherReservation() {
        //Given
        LocalDateTime reservationTime = LocalDateTime.now(UTC);
        Set<Long> reservedTimes = new HashSet<>();
        reservedTimes.add(reservationTime.plusMinutes(130).toEpochSecond(UTC));
        reservedTimes.add(reservationTime.plusMinutes(130).toEpochSecond(UTC));
        Tables table = new Tables(1L, 1);

        //When
        when(reserveDatasourcePort.getReservedTime(anyLong())).thenReturn(reservedTimes);
        when(tableDatasourcePort.getTableById(anyLong())).thenReturn(Optional.of(table));
        reserveTableUseCase.execute(1L, reservationTime);

        //Then
        verify(reserveDatasourcePort, times(1)).setTableReservationTime(anyLong(), any(LocalDateTime.class));
    }


}