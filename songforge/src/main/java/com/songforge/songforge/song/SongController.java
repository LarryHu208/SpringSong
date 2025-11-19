package com.songforge.songforge.song;

import com.songforge.songforge.storage.FileSystemStorageService;
import com.songforge.songforge.audio.FfmpegService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/songs")
public class SongController {

    private final SongJobService service;
    private final FileSystemStorageService storage;
    // Add this field
    private final FfmpegService ffmpegService;

    // Update your constructor to include ffmpegService
    public SongController(SongJobService service, FileSystemStorageService storage, FfmpegService ffmpegService) {
        this.service = service;
        this.storage = storage;
        this.ffmpegService = ffmpegService;
    }

    @PostMapping
    public SongJobDto create(@RequestBody CreateSongRequest req) {
        return SongJobDto.from(service.enqueue(req.prompt(), req.style()));
    }

    @GetMapping("/{id}")
    public SongJobDto get(@PathVariable UUID id) {
        return SongJobDto.from(service.get(id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable UUID id) throws IOException {
        var j = service.get(id);
        if (!"READY".equals(j.getStatus())) {
            return ResponseEntity.status(409)
                    .body(new ByteArrayResource(("Job not READY: " + j.getStatus()).getBytes()));
        }
        Path p = storage.resolve(j.getAudioPath());
        if (!Files.exists(p)) return ResponseEntity.notFound().build();

        byte[] bytes = Files.readAllBytes(p);
        String fmt = j.getAudioFormat() == null ? "wav" : j.getAudioFormat();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/" + fmt))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + j.getId() + "." + fmt + "\"")
                .body(new ByteArrayResource(bytes));
    }

    // Update or add the /render endpoint to fall back to a solid color when no cover exists
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.web.bind.annotation.PostMapping("/{id}/render")
    public org.springframework.http.ResponseEntity<?> render(@org.springframework.web.bind.annotation.PathVariable java.util.UUID id)
            throws java.io.IOException, InterruptedException {

        var job = service.get(id);

        if (job.getAudioPath() == null || job.getAudioPath().isBlank()) {
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.CONFLICT)
                    .body("No audio available to render");
        }

        java.nio.file.Path audio = storage.resolve(job.getAudioPath());
        if (!java.nio.file.Files.exists(audio)) {
            return org.springframework.http.ResponseEntity.notFound().build();
        }

        java.nio.file.Path out = storage.resolve("videos/%s.mp4".formatted(job.getId()));

        java.nio.file.Path cover = null;
        if (job.getCoverPath() != null && !job.getCoverPath().isBlank()) {
            cover = storage.resolve(job.getCoverPath());
        }

        if (cover != null && java.nio.file.Files.exists(cover)) {
            ffmpegService.audioToMp4(audio, cover, out);
        } else {
            ffmpegService.audioToMp4WithColor(audio, "#111827", out); // slate background
        }

        job.setVideoPath("videos/%s.mp4".formatted(job.getId()));
        return org.springframework.http.ResponseEntity.ok().build();
    }
}