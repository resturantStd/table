package com.rst.tableservice.adapter.web.controller;

import com.rst.tableservice.adapter.web.controller.response.TableResponse;
import com.rst.tableservice.usecase.GetTablesUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/api/tables")
public class TableController {

    private final GetTablesUseCase getTablesUseCase;

    public TableController(GetTablesUseCase getTablesUseCase) {
        this.getTablesUseCase = getTablesUseCase;
    }

    @Operation(summary = "Get table by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the book",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = TableResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @GetMapping("/{id}")
    public Mono<TableResponse> getTable(@Parameter(description = "id of table to be searched") @PathVariable Long id) {
        return Mono.just(getTablesUseCase.getById(id))
                .map(TableResponse::from);
    }

    @GetMapping
    public Flux<TableResponse> getTables() {
        return Flux.fromIterable(getTablesUseCase.getAll())
                .map(TableResponse::from);
    }
}