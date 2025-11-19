package com.songforge.songforge.audio.stability;


import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

@Component
@ConditionalOnProperty(name = "app.stableAudio.sub", havingValue = "true", matchIfMissing = true)
public class StableAudioClientStub implements StableAudioClientApi{
    @Override
    public Mono<byte[]> generateSync(String prompt, String style, int durationSec, String outFormat) {
        return Mono.fromCallable(() -> generateSineWave(Math.max(3, durationSec), 44100, 1, 440.0));
    }

    private static byte[] generateSineWave(int seconds, int sampleRate, int channels, double freq) throws Exception{
        int numSamples = seconds * sampleRate;
        var baos = new ByteArrayOutputStream();
        var out = new DataOutputStream(baos);

        int bytesPerSample = 2, byteRate = sampleRate * channels * bytesPerSample;
        int dataSize = numSamples * channels * bytesPerSample, chunkSize = 36 + dataSize;
        out.writeBytes("RIFF"); out.writeInt(Integer.reverseBytes(chunkSize)); out.writeBytes("WAVE");
        out.writeBytes("fmt "); out.writeInt(Integer.reverseBytes(16));
        out.writeShort(Short.reverseBytes((short)1)); // PCM
        out.writeShort(Short.reverseBytes((short)channels));
        out.writeInt(Integer.reverseBytes(sampleRate));
        out.writeInt(Integer.reverseBytes(byteRate));
        out.writeShort(Short.reverseBytes((short)(channels * bytesPerSample)));
        out.writeShort(Short.reverseBytes((short)16));
        out.writeBytes("data"); out.writeInt(Integer.reverseBytes(dataSize));

        double twoPiF = 2 * Math.PI * freq;
        for (int i = 0; i < numSamples; i++) {
            short s = (short)(Math.sin(twoPiF * i / sampleRate) * 32767 * 0.2);
            for (int c = 0; c < channels; c++) out.writeShort(Short.reverseBytes(s));
        }
        out.flush(); return baos.toByteArray();
    }
}
