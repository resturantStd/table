package com.rst.tableservice.usecase;

import com.rst.tableservice.usecase.port.ReserveDatasourcePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static java.time.ZoneOffset.UTC;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetNotAvailableTimeForReservationUseCase {

    private final ReserveDatasourcePort reserveDatasourcePort;

    //Get not available time for reservation
    public List<LocalDateTime> execute(Long tableId) {
        log.info("Get available time for table {}", tableId);
        return reserveDatasourcePort.getReservedTime(tableId)
                .stream()
                .map(time -> LocalDateTime.ofEpochSecond(time, 0, UTC))
                .toList();
    }
}
