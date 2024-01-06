package com.rst.tableservice.infrastructure.db.redis;

import com.rst.tableservice.core.model.TableCondition;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface TableConditionRepository extends ReactiveCrudRepository<TableCondition, String> {
    Mono<TableCondition> getTableConditionByTableId(Long tableId);
}

