package com.rst.tableservice.infrastructure.datasource;

import com.rst.tableservice.core.model.TableStatusType;
import com.rst.tableservice.core.model.Tables;
import com.rst.tableservice.usecase.port.TablesRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class TableDatasource implements TablesRepository {

  private final RedisTemplate<String, String> redisTemplate;

    public TableDatasource(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    @Override
    public Optional<Tables> getTable(Long tableId) {
        return Optional.empty();
    }

    @Override
    public boolean updateStatus(TableStatusType statusType) {
        return false;
    }


    public boolean reserve(Long tableId,LocalDateTime from, LocalDateTime to) {
        return false;
    }

    @Override
    public List<Tables> getAll() {
        return null;
    }
}
