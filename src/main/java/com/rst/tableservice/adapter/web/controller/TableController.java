package com.rst.tableservice.adapter.web.controller;


import com.rst.tableservice.adapter.web.controller.response.TableResponse;
import com.rst.tableservice.usecase.GetTablesUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("tables")
public class TableController {

    private final GetTablesUseCase getTablesUseCase;

    public TableController(GetTablesUseCase getTablesUseCase) {
        this.getTablesUseCase = getTablesUseCase;
    }

    @GetMapping("/{id}")
    public Mono<TableResponse> getTable(@PathVariable Long id) {
        return Mono.just(getTablesUseCase.getById(id))
                .map(TableResponse::from);
    }

    @GetMapping
    public Flux<TableResponse> getTables() {
        return Flux.fromIterable(getTablesUseCase.getAll())
                .map(TableResponse::from);
    }
}
