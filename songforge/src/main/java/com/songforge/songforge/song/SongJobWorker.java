package com.songforge.songforge.song;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SongJobWorker {

    private final SongJobService service;

    public SongJobWorker(SongJobService service) {
        this.service = service;
    }

    @Scheduled(fixedDelay = 1500)
    @Transactional
    public void tick() {
        service.runOneIfQueued();
    }
}
