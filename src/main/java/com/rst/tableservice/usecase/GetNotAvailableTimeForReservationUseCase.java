package com.rst.tableservice.usecase;

import com.rst.tableservice.usecase.port.TableConditionDatasourcePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.ZoneOffset.UTC;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetNotAvailableTimeForReservationUseCase {

    private final TableConditionDatasourcePort tableConditionDatasourcePort;

    //Get not available time for reservation
    public List<LocalDateTime> execute(Long tableId) {
        log.info("Get available time for table {}", tableId);
        return tableConditionDatasourcePort.getReservedTime(tableId)
                .stream()
                .map(time -> LocalDateTime.ofEpochSecond(time, 0, UTC))
                .collect(Collectors.toList());
    }
}
