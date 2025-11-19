package com.songforge.songforge.audio.stability;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Map;

@Component
public class StableAudioClient {

    private final WebClient web;

    public StableAudioClient(
            @Value("${app.stableaudio.apiBase}") String apiBase,
            @Value("${app.stableaudio.apiKey}") String apiKey
    ) {
        this.web = WebClient.builder()
                .baseUrl(apiBase)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    /** Try synchronous “return audio bytes” endpoint. */
    public Mono<byte[]> generateSync(String prompt, String style, int durationSec, String outFormat) {
        // Stable Audio has used /v2beta/stable-audio/generate – keep path configurable here if needed later.
        return web.post()
                .uri("/v2beta/stable-audio/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_OCTET_STREAM, MediaType.valueOf("audio/wav"), MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "prompt", promptWithStyle(prompt, style),
                        "duration", durationSec,
                        "output_format", outFormat   // "wav" or "mp3"
                ))
                .exchangeToMono(resp -> {
                    var ctype = resp.headers().contentType().orElse(MediaType.APPLICATION_OCTET_STREAM);
                    if (ctype.isCompatibleWith(MediaType.APPLICATION_OCTET_STREAM) || ctype.getType().equalsIgnoreCase("audio")) {
                        // Raw bytes body
                        return resp.body(BodyExtractors.toDataBuffers())
                                .reduce((a,b) -> {
                                    ByteBuffer ab = a.asByteBuffer();
                                    ByteBuffer bb = b.asByteBuffer();
                                    ByteBuffer merged = ByteBuffer.allocate(ab.remaining()+bb.remaining());
                                    merged.put(ab).put(bb).flip();
                                    return org.springframework.core.io.buffer.DefaultDataBufferFactory.sharedInstance.wrap(merged);
                                })
                                .map(db -> {
                                    byte[] out = new byte[db.readableByteCount()];
                                    db.read(out);
                                    return out;
                                });
                    } else if (ctype.isCompatibleWith(MediaType.APPLICATION_JSON)) {
                        // Could be base64 or async task; read JSON
                        return resp.bodyToMono(Map.class).flatMap(json -> {
                            // Common patterns:
                            // 1) { "audio": "<base64>" }
                            Object b64 = json.get("audio");
                            if (b64 instanceof String s) {
                                return Mono.just(java.util.Base64.getDecoder().decode(s));
                            }
                            // 2) { "id": "...", "status": "queued"/"done", "download_url": "..." }
                            Object dl = json.get("download_url");
                            if (dl instanceof String url) {
                                return web.get().uri(url).accept(MediaType.APPLICATION_OCTET_STREAM)
                                        .retrieve().bodyToMono(byte[].class);
                            }
                            // 3) { "id": "...", "status_url": "..." } → poll
                            Object statusUrl = json.get("status_url");
                            if (statusUrl instanceof String su) {
                                return pollStatusAndDownload(su);
                            }
                            return Mono.error(new IllegalStateException("Unknown JSON response from Stable Audio"));
                        });
                    } else {
                        return Mono.error(new IllegalStateException("Unsupported content-type: " + ctype));
                    }
                });
    }

    private Mono<byte[]> pollStatusAndDownload(String statusUrl) {
        // Poll up to ~60s
        return Mono.defer(() -> web.get().uri(statusUrl).retrieve().bodyToMono(Map.class))
                .flatMap(j -> {
                    var status = String.valueOf(j.getOrDefault("status",""));
                    if ("done".equalsIgnoreCase(status) || "ready".equalsIgnoreCase(status)) {
                        Object dl = j.get("download_url");
                        if (dl instanceof String url) {
                            return web.get().uri(url).retrieve().bodyToMono(byte[].class);
                        }
                        return Mono.error(new IllegalStateException("No download_url in done status"));
                    }
                    if ("failed".equalsIgnoreCase(status) || "error".equalsIgnoreCase(status)) {
                        return Mono.error(new IllegalStateException("Stable Audio job failed"));
                    }
                    // else queued/running → continue polling
                    return Mono.empty();
                })
                .repeatWhenEmpty( // repeat until emits value
                        r -> r.delayElements(Duration.ofMillis(900)).take(70) // ~63s
                )
                .single(); // error if never produced
    }

    private static String promptWithStyle(String prompt, String style) {
        if (style == null || style.isBlank()) return prompt;
        return prompt + " | style: " + style;
    }
}
