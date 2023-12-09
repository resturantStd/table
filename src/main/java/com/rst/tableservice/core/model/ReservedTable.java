package com.rst.tableservice.core.model;

import java.util.Set;

public record ReservedTable(Long tableId, Set<Long> reservedTimes) {}