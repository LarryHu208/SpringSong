package com.songforge.songforge.song;

import com.songforge.songforge.audio.stability.StableAudioService;
import com.songforge.songforge.storage.FileSystemStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class SongJobService {
    private final SongJobRepository repo;
    private final FileSystemStorageService storage;
    private final StableAudioService stableAudioService;

    public SongJobService(SongJobRepository repo,
                          FileSystemStorageService storage,
                          StableAudioService stableAudioService) {
        this.repo = repo;
        this.storage = storage;
        this.stableAudioService = stableAudioService;
    }

    @Transactional(readOnly = true)
    public SongJob get(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("SongJob not found: " + id));
    }

    // (optional) convenience
    @Transactional
    public SongJob save(SongJob job) {
        return repo.save(job);
    }

    @Transactional
    public SongJob enqueue(String prompt, String style) {
        if (prompt == null || prompt.isBlank() || style == null || style.isBlank()) {
            throw new IllegalArgumentException("prompt and style are required");
        }
        SongJob j = new SongJob();
        j.setId(UUID.randomUUID());
        j.setPrompt(prompt);
        j.setStyle(style);
        j.setStatus("QUEUED");
        return repo.save(j);
    }

    @Transactional
    public SongJob enqueue(String prompt, String style, String idempotencyKey) {

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existing = repo.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                return existing.get(); // return existing job instead of creating duplicate
            }
        }

        SongJob j = new SongJob();
        j.setId(UUID.randomUUID());
        j.setPrompt(prompt);
        j.setStyle(style);
        j.setStatus("QUEUED");
        j.setIdempotencyKey(idempotencyKey);

        return repo.save(j);
    }


    @Transactional
    public void runOneIfQueued() {
        var opt = repo.findFirstByStatusOrderByCreatedAtAsc("QUEUED");
        if (opt.isEmpty()) return;

        var job = opt.get();
        job.setStatus("RUNNING");
        repo.save(job);

        try {
            // inside your job processing code:
            var fmt = (job.getAudioFormat() == null || job.getAudioFormat().isBlank()) ? "wav" : job.getAudioFormat();
            var out = storage.resolve("songs/%s.%s".formatted(job.getId(), fmt));

            // Call Stable Audio
            stableAudioService.generateTo(out, job.getPrompt(), job.getStyle(), /*duration*/30, fmt);

            // Record path + mark READY
            job.setAudioFormat(fmt);
            job.setAudioPath("songs/%s.%s".formatted(job.getId(), fmt));
            job.setStatus("READY");
            job.setError(null);
            repo.save(job);
        } catch (Exception e) {
            job.setStatus("FAILED");
            job.setError(e.getMessage());
            repo.save(job);
        }
    }

    @Transactional(readOnly = true)
    public List<SongJob> listAll() {
        return repo.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<SongJob> listByStatus(String status) {
        return repo.findByStatusOrderByCreatedAtDesc(status);
    }
}