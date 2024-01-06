package com.rst.tableservice.usecase.port;

import com.rst.tableservice.core.model.TableCondition;
import com.rst.tableservice.core.model.TableStatusType;
import reactor.core.publisher.Mono;

public interface TableConditionDatasourcePort {
    void updateStatus(TableStatusType statusType, long tableId);

    Mono<TableCondition> getTableConditionByTableId(long tableId);

    Mono<TableCondition> save(TableCondition tableCondition);
}
