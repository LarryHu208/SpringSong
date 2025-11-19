package com.songforge.songforge.audio.stability;

import reactor.core.publisher.Mono;

public interface StableAudioClientApi {
    Mono<byte[]> generateSync(String prompt, String style, int durationSec, String outFormat);

}
