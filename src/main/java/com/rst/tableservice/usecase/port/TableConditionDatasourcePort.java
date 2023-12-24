package com.rst.tableservice.usecase.port;

import com.rst.tableservice.core.model.TableCondition;
import com.rst.tableservice.core.model.TableStatusType;

import java.util.Optional;

public interface TableConditionDatasourcePort {
    void updateStatus(TableStatusType statusType, long tableId);

    Optional<TableCondition> getTableConditionByTableId(long tableId);

    void save(TableCondition tableCondition);
}
