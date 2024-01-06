package com.rst.tableservice.adapter.web.controller;

import com.rst.tableservice.usecase.OccupiedTableUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/api")
@RequiredArgsConstructor
public class OccupationTableController {

    private final OccupiedTableUseCase occupiedTableUseCase;

    @GetMapping("/occupy")
    public Mono<Void> occupyTable(@RequestParam Long tableId) {
        return occupiedTableUseCase.execute(tableId);
    }
}
