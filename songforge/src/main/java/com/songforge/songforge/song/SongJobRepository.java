package com.songforge.songforge.song;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SongJobRepository extends JpaRepository<SongJob, UUID> {
    Optional<SongJob> findFirstByStatusOrderByCreatedAtAsc(String status);
}
