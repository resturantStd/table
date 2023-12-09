package com.rst.tableservice.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RedisHash("TableCondition")
public class TableCondition {

    @Id
    private String id;
    private long tableId;
    private boolean occupied;
    private String location;
    TableStatusType status;

}