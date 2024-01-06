package com.rst.tableservice.usecase;

import com.rst.tableservice.core.exception.TableNotAvailableException;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Set;

import static java.time.ZoneOffset.UTC;
import static org.mockito.Mockito.*;

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
        when(tableConditionDatasourcePort.getTableConditionByTableId(tableId)).thenReturn(Mono.just(tableCondition));

        when(tableConditionDatasourcePort.save(any(TableCondition.class))).thenReturn(Mono.just(tableCondition).flatMap(tableCondition1 -> {
            tableCondition1.setStatus(TableStatusType.OCCUPIED);
            tableCondition1.setOccupied(true);
            return Mono.just(tableCondition1);
        }));

        //Then
        StepVerifier.create(occupiedTableUseCase.execute(tableId))
                .verifyComplete();
        verify(tableConditionDatasourcePort, times(1)).save(any(TableCondition.class));
    }

    @Test
    @DisplayName("Should execute when table is not present")
    void shouldExecuteWhenTableIsNotPresent() {
        //Given
        long tableId = 1L;

        //When
        when(tableConditionDatasourcePort.getTableConditionByTableId(tableId)).thenReturn(Mono.empty());
        when(tableConditionDatasourcePort.save(any(TableCondition.class))).thenReturn(Mono.just(TableCondition.builder()
                .tableId(tableId)
                .occupied(true)
                .status(TableStatusType.OCCUPIED)
                .build()));

        //Then
        StepVerifier.create(occupiedTableUseCase.execute(tableId))
                .verifyComplete();
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
        when(tableConditionDatasourcePort.getTableConditionByTableId(tableId)).thenReturn(Mono.just(tableCondition));
        when(reserveDatasourcePort.getReservedTime(tableId)).thenReturn(reservationTimes);
        when(tableConditionDatasourcePort.save(any(TableCondition.class))).thenReturn(Mono.just(tableCondition)
                .flatMap(tableCondition1 -> {
            tableCondition1.setStatus(TableStatusType.OCCUPIED);
            tableCondition1.setOccupied(true);
            return Mono.just(tableCondition1);
        }));

        //Then
        StepVerifier.create(occupiedTableUseCase.execute(tableId))
                .verifyComplete();
        verify(tableConditionDatasourcePort, times(1)).save(any(TableCondition.class));
    }

    @Test
    @DisplayName("Should execute when table is not occupied and reserved but no reservation time")
    void shouldExecuteWhenTableIsNotOccupiedAndReservedButNoReservationTime() {
        //Given
        long tableId = 1L;
        Set<Long> reservationTimes = Set.of();
        Mono<TableCondition> tableCondition = Mono.just(TableCondition.builder()
                .tableId(tableId)
                .occupied(false)
                .status(TableStatusType.RESERVED)
                .build());

        //When
        when(tableConditionDatasourcePort.getTableConditionByTableId(tableId)).thenReturn(tableCondition);
        when(reserveDatasourcePort.getReservedTime(tableId)).thenReturn(reservationTimes);


        when(tableConditionDatasourcePort.save(any(TableCondition.class))).thenReturn(tableCondition.flatMap(tableCondition1 -> {
            tableCondition1.setStatus(TableStatusType.OCCUPIED);
            tableCondition1.setOccupied(true);
            return Mono.just(tableCondition1);
        }));


        //Then
        StepVerifier.create(occupiedTableUseCase.execute(tableId))
                .verifyComplete();
        verify(tableConditionDatasourcePort, times(1)).save(any(TableCondition.class));
    }

    @Test
    @DisplayName("Should throw exception when table is occupied")
    void shouldThrowExceptionWhenTableIsOccupied() {
        //Given
        long tableId = 1L;
        TableCondition tableCondition = TableCondition.builder()
                .tableId(tableId)
                .occupied(true)
                .status(TableStatusType.OCCUPIED)
                .build();

        //When
        when(tableConditionDatasourcePort.getTableConditionByTableId(tableId)).thenReturn(Mono.just(tableCondition));

        //Then
        StepVerifier.create(occupiedTableUseCase.execute(tableId))
                .expectError(TableNotAvailableException.class)
                .verify();
    }

    @Test
    @DisplayName("Should throw exception when table is reserved and reservation times are within 2 hours")
    void shouldThrowExceptionWhenTableIsReservedAndReservationTimesAreWithinTwoHours() {
        //Given
        long tableId = 1L;
        Set<Long> reservationTimes = Set.of(
                                        LocalDateTime.now(UTC).minusHours(2).toEpochSecond(UTC),
                                        LocalDateTime.now(UTC).plusHours(2).toEpochSecond(UTC)
                                    );
        //reservation time is now and the client is occupying the table
        TableCondition tableCondition = TableCondition.builder()
                .tableId(tableId)
                .occupied(true)
                .status(TableStatusType.RESERVED)
                .build();

        //When
        when(tableConditionDatasourcePort.getTableConditionByTableId(tableId)).thenReturn(Mono.just(tableCondition));
        when(reserveDatasourcePort.getReservedTime(tableId)).thenReturn(reservationTimes);

        //Then
        StepVerifier.create(occupiedTableUseCase.execute(tableId))
                .expectError(TableNotAvailableException.class)
                .verify();
    }


    @Test
    @DisplayName("Should not throw exception when table is reserved and reservation times are within 2 hours but table is not occupied")
    void shouldNotThrowExceptionWhenTableIsReservedAndReservationTimesAreWithinTwoHours() {
        //Given
        long tableId = 1L;
        Set<Long> reservationTimes = Set.of(
                //table is reserved but not occupied yet
                //reservation time is off after 30 minutes
                LocalDateTime.now(UTC).minusHours(1).minusMinutes(31).toEpochSecond(UTC),
                LocalDateTime.now(UTC).plusHours(2).toEpochSecond(UTC)
        );

        TableCondition tableCondition = TableCondition.builder()
                .tableId(tableId)
                .occupied(false)
                .status(TableStatusType.RESERVED)
                .build();

        //When
        when(tableConditionDatasourcePort.getTableConditionByTableId(tableId)).thenReturn(Mono.just(tableCondition));
        when(reserveDatasourcePort.getReservedTime(tableId)).thenReturn(reservationTimes);
        when(tableConditionDatasourcePort.save(any(TableCondition.class))).thenReturn(Mono.just(tableCondition).flatMap(tableCondition1 -> {
            tableCondition1.setStatus(TableStatusType.RESERVED);
            tableCondition1.setOccupied(true);
            return Mono.just(tableCondition1);
        }));

        //Then
        StepVerifier.create(occupiedTableUseCase.execute(tableId))
                .verifyComplete();
        verify(tableConditionDatasourcePort, times(1)).save(any(TableCondition.class));
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
        when(tableConditionDatasourcePort.getTableConditionByTableId(tableId)).thenReturn(Mono.just(tableCondition));
        when(reserveDatasourcePort.getReservedTime(tableId)).thenReturn(reservationTimes);

        //Then
        StepVerifier.create(occupiedTableUseCase.execute(tableId))
                .expectError(TimeNotAvailableException.class)
                .verify();
    }

    @Test
    public void testexecute_throwsTimeNotAvailableException() {
        // Arrange
        long tableId = 1;
        TableCondition tableCondition = TableCondition.builder()
                .tableId(tableId)
                .occupied(false)
                .status(TableStatusType.RESERVED)
                .build();
        when(tableConditionDatasourcePort.getTableConditionByTableId(tableId)).thenReturn(Mono.just(tableCondition));
        LocalDateTime reservationTime = LocalDateTime.now(UTC).minusMinutes(20);
        when(reserveDatasourcePort.getReservedTime(tableId)).thenReturn(Set.of(reservationTime.toEpochSecond(UTC)));

        // Act & Assert
        StepVerifier.create(occupiedTableUseCase.execute(tableId))
                .expectError(TimeNotAvailableException.class)
                .verify();
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
        when(tableConditionDatasourcePort.getTableConditionByTableId(tableId)).thenReturn(Mono.just(tableCondition));
        when(reserveDatasourcePort.getReservedTime(tableId)).thenReturn(reservationTimes);

        //Then
        StepVerifier.create(occupiedTableUseCase.execute(tableId))
                .expectError(TimeNotAvailableException.class)
                .verify();
    }
}