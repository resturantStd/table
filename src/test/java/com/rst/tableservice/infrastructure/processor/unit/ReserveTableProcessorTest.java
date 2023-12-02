package com.rst.tableservice.infrastructure.processor.unit;

import com.rst.tableservice.infrastructure.processor.ReserveTableProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ReserveTableProcessorTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @InjectMocks
    private ReserveTableProcessor reserveTableProcessor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    @Test
    public void shouldRemoveReservationWhenTimeIsGrateThanCurrentTime() {
        Set<Long> reserveTimeSet = new HashSet<>();
        long time = LocalDateTime.now().plusMinutes(35).toEpochSecond(ZoneOffset.UTC);
        reserveTimeSet.add(time);
        when(hashOperations.entries("table.reserve"))
                .thenReturn(Map.of(1L, reserveTimeSet));

        reserveTableProcessor.execute();

        verify(hashOperations).delete(eq("table.reserve"), eq(1L));
    }

    @Test
    public void shouldNotRemoveReservationWhenTimeIsCurrentTime() {
        Set<Long> reserveTimeSet = new HashSet<>();
        reserveTimeSet.add(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        when(hashOperations.entries("table.reserve"))
                .thenReturn(Map.of(1L, reserveTimeSet));

        reserveTableProcessor.execute();

        verify(hashOperations, never()).delete(any(), any());
    }

    @Test
    public void shouldNotRemoveReservationWhenNoReservationsExist() {
        when(hashOperations.entries("table.reserve")).thenReturn(Map.of());

        reserveTableProcessor.execute();

        verify(hashOperations, never()).delete(any(), any());
    }
}