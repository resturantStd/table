package com.rst.tableservice.infrastructure.datasource;

import com.google.gson.Gson;
import com.rst.tableservice.core.exception.TableNotFoundException;
import com.rst.tableservice.core.exception.TimeNotAvailableException;
import com.rst.tableservice.core.model.TableStatusType;
import com.rst.tableservice.core.model.Tables;
import com.rst.tableservice.usecase.port.TablesRepository;
import lombok.val;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;

import static java.time.ZoneOffset.UTC;

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
    public void updateStatus(TableStatusType statusType, Long tableId) {
        val tableKey = TABLE_KEY + tableId;
        var table = Optional.ofNullable(
                redisTemplate.opsForValue().get(tableKey))
                .map(str -> GSON.fromJson(str, Tables.class))
                    .orElseThrow(() -> new TableNotFoundException(tableId));

        table.setStatus(statusType);
        redisTemplate.opsForValue().set(tableKey, GSON.toJson(table));
    }

    @Override
    public void reserve(Long tableId, LocalDateTime from) {
        val tableKey = TABLE_KEY + tableId;
        val table = Optional.ofNullable(redisTemplate.opsForValue().get(tableKey))
                .map(str -> GSON.fromJson(str, Tables.class))
                .orElseThrow(() -> new TableNotFoundException(tableId));

        table.setStatus(TableStatusType.RESERVED);
        redisTemplate.opsForValue().set(tableKey, GSON.toJson(table));

        var timeSet = Optional.ofNullable(redisTemplate.opsForHash().get("table.reserve", tableId))
                .map(obj -> (Set<Long>) obj)
                .orElse(new HashSet<>());

        if (!CollectionUtils.isEmpty(timeSet)) {
            validateIsTimeAvailable(from, timeSet, tableId);
        }

        timeSet.add(from.toEpochSecond(UTC));
        redisTemplate.opsForHash().put("table.reserve", tableId, timeSet);
    }


    /*
    check if the time is available time is 120 minutes before from time and 120 minutes after from time
    if the time is not available then throw exception
    if the time is not available then throw exception
    */
    private void validateIsTimeAvailable(LocalDateTime from, Set<Long> timeSet, long tableId) {
        timeSet.stream()
                .map(time -> LocalDateTime.ofEpochSecond(time, 0, UTC))
                .filter(triggerTime -> triggerTime.isAfter(from.minusMinutes(120)) && triggerTime.isBefore(from.plusMinutes(120)))
                .findFirst()
                .ifPresent(triggerTime -> {
                    throw new TimeNotAvailableException(tableId);
                });
    }

    @Override
    public List<Tables> getAll() {
        return Objects.requireNonNull(redisTemplate.keys(TABLE_KEY + "*")).stream()
                .map(key -> redisTemplate.opsForValue().get(key))
                .map(str -> GSON.fromJson(str, Tables.class))
                .toList();
    }
}
