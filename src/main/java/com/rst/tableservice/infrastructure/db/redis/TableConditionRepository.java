package com.rst.tableservice.infrastructure.db.redis;

import com.rst.tableservice.core.model.TableCondition;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TableConditionRepository extends CrudRepository<TableCondition, String> {


    Optional<TableCondition> getTableConditionByTableId(Long tableId);
}
