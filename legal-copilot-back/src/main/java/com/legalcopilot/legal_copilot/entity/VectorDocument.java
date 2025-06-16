package com.legalcopilot.legal_copilot.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "vector_store")
public class VectorDocument {

    @Id
    private UUID id;

    @Column
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Column(columnDefinition = "vector(1536)", nullable = false)
    private float[] embedding;

    @PrePersist
    private void prePersist() {
        this.id = UUID.randomUUID();
    }

}
