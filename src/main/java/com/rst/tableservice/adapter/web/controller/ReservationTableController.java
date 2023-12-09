package com.rst.tableservice.adapter.web.controller;

import com.rst.tableservice.usecase.GetNotAvailableTimeForReservationUseCase;
import com.rst.tableservice.usecase.ReserveTableUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/v1/api")
@RequiredArgsConstructor
public class ReservationTableController {

    private final ReserveTableUseCase reserveTableUseCase;
    private final GetNotAvailableTimeForReservationUseCase getNotAvailableTimeForReservationUseCase;

    @GetMapping("/reserve")
    public Mono<Void> reserveTable(@RequestParam Long tableId,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from ) {
        return Mono.fromRunnable(() -> reserveTableUseCase.execute(tableId, from));
    }


    @GetMapping("/not-available-time")
    public Flux<LocalDateTime> getNotAvailableTime(@RequestParam Long tableId) {
        return Flux.fromIterable(getNotAvailableTimeForReservationUseCase.execute(tableId));
    }
}
