package com.rst.tableservice.infrastructure.datasource;

import com.rst.tableservice.core.exception.TableNotFoundException;
import com.rst.tableservice.core.model.ReservedTable;
import com.rst.tableservice.usecase.port.ReserveDatasourcePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.time.ZoneOffset.UTC;

@Repository
@RequiredArgsConstructor
public class ReserveDatasource implements ReserveDatasourcePort {

    private static final String TABLE_RESERVED_KEY = "table.reserved.status";
    private final RedisTemplate<String, String> redisTemplate;




    @Override
    public void setTableReservationTime(long tableId, LocalDateTime time) {
        Optional.ofNullable(redisTemplate.opsForHash().get(TABLE_RESERVED_KEY, tableId))
                .map(timeSet -> (Set<Long>) timeSet)
                .ifPresentOrElse(timeSet -> {
                    timeSet.add(time.toEpochSecond(UTC));
                    redisTemplate.opsForHash().put(TABLE_RESERVED_KEY, tableId, timeSet);
                }, () -> {
                    throw new TableNotFoundException(tableId);
                });
    }

    @Override
    public Set<Long> getReservedTime(long tableId) {
        return Optional.ofNullable(redisTemplate.opsForHash().get(TABLE_RESERVED_KEY, tableId))
                .map(time -> (Set<Long>) time)
                .orElseGet(HashSet::new);
    }



    @Override
    public void deleteReservedTime(long tableId, LocalDateTime time) {
        Optional.ofNullable(redisTemplate.opsForHash().get(TABLE_RESERVED_KEY, tableId))
                .map(timeSet -> (Set<Long>) timeSet)
                .ifPresentOrElse(timeSet -> {
                    boolean removed = timeSet.remove(time.toEpochSecond(UTC));
                    if (!removed) {
                        throw new RuntimeException("Time not found");
                    }
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
}
