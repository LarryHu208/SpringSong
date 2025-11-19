package com.songforge.songforge.audio;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class MinimalWav {
    private MinimalWav() {}

    public static byte[] silenceSeconds(int seconds) {
        int sampleRate = 44100;
        int channels = 2;
        int bitsPerSample = 16;
        int bytesPerSample = bitsPerSample / 8;
        int dataBytes = seconds * sampleRate * channels * bytesPerSample;

        ByteArrayOutputStream out = new ByteArrayOutputStream(44 + dataBytes);
        try {
            out.write("RIFF".getBytes());
            out.write(le(36 + dataBytes));
            out.write("WAVE".getBytes());
            out.write("fmt ".getBytes());
            out.write(le(16));              // PCM header size
            out.write(le((short)1));        // PCM
            out.write(le((short)channels));
            out.write(le(sampleRate));
            out.write(le(sampleRate * channels * bytesPerSample)); // byte rate
            out.write(le((short)(channels * bytesPerSample)));     // block align
            out.write(le((short)bitsPerSample));
            out.write("data".getBytes());
            out.write(le(dataBytes));
            // data = zeros (silence)
            out.write(new byte[dataBytes]);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] le(int v) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(v).array();
    }
    private static byte[] le(short v) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(v).array();
    }
}
