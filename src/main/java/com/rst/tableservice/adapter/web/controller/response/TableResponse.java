package com.rst.tableservice.adapter.web.controller.response;

import com.rst.tableservice.core.model.Tables;

public record TableResponse(Long id) {
    public static TableResponse from(Tables tables) {
        return tables == null ? null : new TableResponse(tables.getId());
    }
}
