package com.rst.tableservice.infrastructure.datasource;

import com.rst.tableservice.core.exception.TableNotFoundException;
import com.rst.tableservice.core.exception.TimeNotAvailableException;
import com.rst.tableservice.core.model.ReservedTable;
import com.rst.tableservice.core.model.TableCondition;
import com.rst.tableservice.infrastructure.db.jdbc.TableRepository;
import com.rst.tableservice.infrastructure.db.redis.TableConditionRepository;
import com.rst.tableservice.usecase.port.TableConditionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.ZoneOffset.UTC;

@Repository
@RequiredArgsConstructor
public class TableConditionDatasource implements TableConditionPort {

    private static final String TABLE_RESERVED_KEY = "table.reserved.status";
    private final RedisTemplate<String, String> redisTemplate;
    private final TableRepository tableRepository;
    private final TableConditionRepository tableConditionRepository;


    @Override
    public void reserve(Long tableId, LocalDateTime from) {
        tableRepository.findById(tableId)
                .ifPresentOrElse(table -> {
                    Set<Long> reservedTime = Optional.ofNullable(redisTemplate.opsForHash().get(TABLE_RESERVED_KEY, tableId))
                            .map(time -> (Set<Long>) time)
                            .orElseGet(HashSet::new);

                    validateIsTimeAvailable(from, reservedTime, tableId);
                    tableConditionRepository.getTableConditionByTableId(tableId)
                            .ifPresentOrElse(tableCondition -> {
                                tableCondition.setOccupied(true);
                                tableConditionRepository.save(tableCondition);
                                reservedTime.add(from.toEpochSecond(UTC));
                            }, () -> {
                                var tableCondition = new TableCondition();
                                tableCondition.setTableId(tableId);
                                tableCondition.setOccupied(true);
                                tableConditionRepository.save(tableCondition);
                                reservedTime.add(from.toEpochSecond(UTC));
                            });
                    redisTemplate.opsForHash().put(TABLE_RESERVED_KEY, tableId, reservedTime);
                }, () -> {
                    throw new RuntimeException("Table not found");
                });
    }

    @Override
    public Set<Long> getReservedTime(Long tableId) {
        return Optional.ofNullable(redisTemplate.opsForHash().get(TABLE_RESERVED_KEY, tableId))
                .map(time -> (Set<Long>) time)
                .orElseGet(HashSet::new);
    }

    @Override
    public void deleteReservedTime(Long tableId, LocalDateTime time) {
        Optional.ofNullable(redisTemplate.opsForHash().get(TABLE_RESERVED_KEY, tableId))
                .map(timeSet -> (Set<Long>) timeSet)
                .ifPresentOrElse(timeSet -> {
                    timeSet.remove(time.toEpochSecond(UTC));
                    redisTemplate.opsForHash().put(TABLE_RESERVED_KEY, tableId, timeSet);
                }, () -> {
                    throw new TableNotFoundException(tableId);
                });
    }

    @Override
    public List<ReservedTable> getAllReservedTable() {
        return redisTemplate.<Long, Set<Long>>opsForHash()
                .entries(TABLE_RESERVED_KEY)
                .entrySet()
                .stream()
                .map(entry -> {
                    try {
                        return new ReservedTable(entry.getKey(), entry.getValue());
                    } catch (ClassCastException e) {
                        throw new RuntimeException("Invalid data in Redis", e);
                    }
                })
                .collect(Collectors.toList());
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
}
