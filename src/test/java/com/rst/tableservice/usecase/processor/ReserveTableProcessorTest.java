package com.rst.tableservice.usecase.processor;

import com.rst.tableservice.core.model.ReservedTable;
import com.rst.tableservice.core.model.TableStatusType;
import com.rst.tableservice.usecase.port.ReserveDatasourcePort;
import com.rst.tableservice.usecase.port.TableConditionDatasourcePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static java.time.ZoneOffset.UTC;
import static org.mockito.Mockito.*;


/*
* TODO
*   доработать теты
* необходимо проверить что:
*  1. если время резерва больше 30 минут и стол не поменял статус на OCCUPAID, то столик освобождается
*  2. если время резерва больше 30 минут и стол поменял статус на  OCCUPAID, то столик не освобождается
*  3. если время резерва меньше 29 минут  то сол меняет флаг  что он занят на  true  (за 30 минут до резервирования стол меняет статус на OCCUPAID)
* */
class ReserveTableProcessorTest {

    @Mock
    private TableConditionDatasourcePort tableConditionPort;
    @Mock
    private ReserveDatasourcePort reserveDatasourcePort;

    @InjectMocks
    private ReserveTableProcessor reserveTableProcessor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Unreserved table when reservation time is more than 30 minutes")
    void shouldUnreserveTableWhenReservationTimeIsMoreThan30Minutes() {
        //Given
        Set<Long> reservationTimes = Set.of(LocalDateTime.now(UTC).minusMinutes(30).toEpochSecond(UTC));
        Long table1 = 1L;
        ReservedTable reservedTable = new ReservedTable(table1, reservationTimes);
        when(reserveDatasourcePort.getAllReservedTable()).thenReturn(List.of(reservedTable));

        //When
        reserveTableProcessor.execute();

        //Then
        LocalDateTime expTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(reservationTimes.iterator().next()), UTC);
        verify(reserveDatasourcePort, times(1)).deleteReservedTime(reservedTable.tableId(), expTime);
        verify(tableConditionPort, times(1)).updateStatus(TableStatusType.AVAILABLE, reservedTable.tableId());
    }

    @Test
    @DisplayName("Mark table as occupied when reservation time is before 30 minutes")
    void shouldMarkTableAsOccupiedWhenReservationTimeIsWithin30Minutes() {
        //Given
        Set<Long> reservationTimes =  Set.of(LocalDateTime.now(UTC).plusMinutes(29).toEpochSecond(UTC));
        Long table1 = 1L;
        ReservedTable reservedTable = new ReservedTable(table1, reservationTimes);

        //When
        when(reserveDatasourcePort.getAllReservedTable()).thenReturn(List.of(reservedTable));
        reserveTableProcessor.execute();

        //Then
        verify(tableConditionPort, times(1)).updateStatus(TableStatusType.OCCUPIED, reservedTable.tableId());
    }

    @Test
    @DisplayName("Do not change table status when reservation time is less than 30 minutes")
    void shouldNotChangeTableStatusWhenReservationTimeIsLessThan30Minutes() {
        //Given
        Set<Long> reservationTimes = Set.of(LocalDateTime.now(UTC).minusMinutes(35).toEpochSecond(UTC));
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