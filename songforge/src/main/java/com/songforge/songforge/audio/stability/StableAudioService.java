package com.songforge.songforge.audio.stability;

import com.songforge.songforge.storage.FileSystemStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.scheduler.Schedulers;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class StableAudioService {

    //private final StableAudioClientApi client;
    private final StableAudioClient client;
    private final FileSystemStorageService storage;
    private final int defaultDuration;
    private final String defaultFormat;

    public StableAudioService(
            StableAudioClient client,
            FileSystemStorageService storage,
            @Value("${app.stableaudio.durationSec:30}") int defaultDuration,
            @Value("${app.stableaudio.outputFormat:wav}") String defaultFormat
    ) {
        this.client = client;
        this.storage = storage;
        this.defaultDuration = defaultDuration;
        this.defaultFormat = defaultFormat;
    }

    /** Generates audio and writes to `songs/<id>.<fmt>` under your storage base. */
    public Path generateTo(Path outPath, String prompt, String style, Integer durationSec, String fmt) throws Exception {
        int dur = (durationSec == null || durationSec <= 0) ? defaultDuration : durationSec;
        String format = (fmt == null || fmt.isBlank()) ? defaultFormat : fmt;

        Files.createDirectories(outPath.getParent());

        byte[] audio = client.generateSync(prompt, style, dur, format)
                .publishOn(Schedulers.boundedElastic())
                .block(); // your pipeline is already job-based; blocking here is fine in the worker

        Files.write(outPath, audio);
        return outPath;
    }
}
