package com.rst.tableservice.infrastructure.datasource.process;

import lombok.val;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

@Component
public class ReserveTableProcessor {


    /*
    * Documentation
    *
    * Reserve table if time is less than 30 minutes
    *  and remove the reservation after 30 minutes if the table is not occupied.
    *
    * Key in REDIS "table.reserve" is a hash table with 'key' as table id
    *  and 'value' as SET of times in seconds when the table is reserved.
    */
    private final RedisTemplate<String, String> redisTemplate;

    public ReserveTableProcessor(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(fixedDelay = 1000)
    public void process() {
        redisTemplate.opsForHash().entries("table.reserve").entrySet().forEach(entry -> {
            val key = (Long) entry.getKey();
            val value = (Set<Long>)entry.getValue();
            value.forEach(v -> {
                if (v < TimeoutUtils.toSeconds(Duration.ofDays(LocalDateTime.now().getMinute()))) {
                    redisTemplate.opsForHash().delete("table.reserve", key);
                }

        });
        });
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
