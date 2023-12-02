package com.rst.tableservice.infrastructure.datasource;

import com.google.gson.Gson;
import com.rst.tableservice.core.exception.TableNotFoundException;
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
    private static final String TABLE_KEY = "table.id.";
    private static final Gson GSON = new Gson();

    private final RedisTemplate<String, String> redisTemplate;

    public TableDatasource(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    @Override
    public Optional<Tables> getTable(Long tableId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(TABLE_KEY + tableId))
                .map(str -> GSON.fromJson(str, Tables.class));
    }

    @Override
    public boolean updateStatus(TableStatusType statusType, Long tableId) {
        Optional.ofNullable(redisTemplate.opsForValue().get(TABLE_KEY + tableId))
                .map(str -> GSON.fromJson(str, Tables.class)).ifPresentOrElse(v -> {
                            v.setStatus(statusType);
                            redisTemplate.opsForValue().set(TABLE_KEY + tableId, GSON.toJson(v));
                        },
                        () -> {
                            throw new TableNotFoundException(tableId);
                        });
        return true;
    }

//Reserve a table for a given time
    public boolean reserve(Long tableId, LocalDateTime from, LocalDateTime to) {
        Optional.ofNullable(redisTemplate.opsForValue().get(TABLE_KEY + tableId))
                .map(str -> GSON.fromJson(str, Tables.class)).ifPresentOrElse(v -> {
                            v.setStatus(TableStatusType.RESERVED);
                            redisTemplate.opsForValue().set(TABLE_KEY + tableId, GSON.toJson(v), to.minusMinutes(from.getMinute()).getMinute());
                        },
                        () -> {
                            throw new TableNotFoundException(tableId);
                        });
        return true;
    }

    @Override
    public List<Tables> getAll() {
        return null;
    }
}
