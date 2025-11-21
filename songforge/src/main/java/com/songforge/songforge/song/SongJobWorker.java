package com.songforge.songforge.song;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SongJobWorker {

    private final SongJobService service;

    public SongJobWorker(SongJobService service) {
        this.service = service;
    }

    @Scheduled(fixedDelayString = "${app.jobs.pollDelayMs:1000}")
    public void tick() {
        service.runOneIfQueued();
    }
}
