package com.rst.tableservice.infrastructure.processor.unit;

public class ReserveTableProcessorTest {
/*
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
    }*/
}