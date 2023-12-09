package com.rst.tableservice.infrastructure.db.jdbc;

import com.rst.tableservice.core.model.Tables;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TableRepository extends JpaRepository<Tables, Long> {
}