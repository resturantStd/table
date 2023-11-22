package com.rst.tableservice.adapter.web.controller;


import com.rst.tableservice.adapter.web.controller.response.TableResponse;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class TableController {

        public Mono<TableResponse> getTable(Long id) {
            //TODO: implement
            return Mono.empty();
        }

        public Flux<TableResponse> getTables() {
            //TODO: implement
            return Flux.empty();
        }
}
