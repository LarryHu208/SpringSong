package com.example.demo.snippet;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
public class Snippet {
    @Id
    private UUID id = UUID.randomUUID();
    @Column(nullable = false, length = 80) private String title;
    @Lob @Column (nullable = false) private String code;
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
