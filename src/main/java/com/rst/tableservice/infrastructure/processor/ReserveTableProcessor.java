package com.rst.tableservice.infrastructure.processor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import static java.time.ZoneOffset.UTC;


/*
 * Documentation
 *
 * Reserve table if time is less than 30 minutes
 *  and remove the reservation after 30 minutes if the table is not occupied.
 *
 * Key in REDIS "table.reserve" is a hash table with 'key' as table id
 *  and 'value' as SET of times in seconds when the table is reserved.
 */

@Slf4j
@Component
public class ReserveTableProcessor {
    private final RedisTemplate<String, String> redisTemplate;

    public ReserveTableProcessor(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
/*
    @Scheduled(fixedDelay = 1000)
    public void execute() {
        log.info("process check reserved table is started {}", new Date());
        Map<Object, Object> entries = redisTemplate.opsForHash().entries("table.reserve");
        entries.forEach((key, value) -> {
            val tableId = (Long) key;
            val reserveTimeSet = (Set<Long>) value;
            reserveTimeSet.forEach(time -> {
                val triggerTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(time), UTC);
                if (triggerTime.isAfter(LocalDateTime.now().plusMinutes(30))) {
                    redisTemplate.opsForHash().delete("table.reserve", tableId);
                    log.info("tabel {} was unreserved", tableId);
                }
            });
        });
    }*/
}
