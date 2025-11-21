package com.songforge.songforge.song;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "song_job")
@Data // generates getters/setters, equals/hashCode, toString
public class SongJob {

    private String coverPath;
    private String videoPath;

    @Id
    private UUID id;

    @Column(nullable = false)
    private String prompt;

    private String style;

    @Column(nullable = false)
    private String status; // QUEUED|RUNNING|READY|FAILED|PUBLISHED

    @Column(unique = true)
    private String idempotencyKey;

    private String audioPath;    // e.g., songs/{id}.wav
    private String audioFormat;  // "wav"
    @Column(length = 2000)
    private String error;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        createdAt = OffsetDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
