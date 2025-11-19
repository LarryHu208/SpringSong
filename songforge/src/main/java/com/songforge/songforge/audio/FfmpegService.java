package com.songforge.songforge.audio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

@Service
public class FfmpegService {

    @Value("${app.ffmpeg.path:ffmpeg}")
    private String ffmpeg;

    @Value("${app.ffmpeg.size:1280x720}")
    private String size; // e.g., 1280x720

    @Value("${app.ffmpeg.fps:30}")
    private int fps; // some players prefer a nominal FPS

    @Value("${app.ffmpeg.timeoutSecs:60}")
    private int timeoutSecs;

    public Path audioToMp4(Path audio, Path coverImage, Path outMp4)
            throws IOException, InterruptedException {
        Files.createDirectories(outMp4.getParent());
        List<String> cmd = List.of(
                ffmpeg, "-y",
                "-loop", "1", "-i", coverImage.toString(),
                "-i", audio.toString(),
                "-r", String.valueOf(fps),
                "-c:v", "libx264", "-tune", "stillimage",
                "-vf", "scale=" + size + ":force_original_aspect_ratio=decrease,pad=" + size + ":(ow-iw)/2:(oh-ih)/2",
                "-c:a", "aac", "-b:a", "192k",
                "-pix_fmt", "yuv420p",
                "-shortest",
                outMp4.toString()
        );
        run(cmd, Duration.ofSeconds(timeoutSecs));
        return outMp4;
    }

    public Path audioToMp4WithColor(Path audio, String hexColor, Path outMp4)
            throws IOException, InterruptedException {
        Files.createDirectories(outMp4.getParent());
        String c = normalizeHex(hexColor); // accepts "#111827" or "111827"
        List<String> cmd = List.of(
                ffmpeg, "-y",
                "-f", "lavfi", "-i", "color=c=#" + c + ":s=" + size + ":r=" + fps,
                "-i", audio.toString(),
                "-c:v", "libx264", "-tune", "stillimage",
                "-c:a", "aac", "-b:a", "192k",
                "-pix_fmt", "yuv420p",
                "-shortest",
                outMp4.toString()
        );
        run(cmd, Duration.ofSeconds(timeoutSecs));
        return outMp4;
    }

    // --- helpers ---

    private void run(List<String> cmd, Duration timeout) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true); // merge stderr into stdout
        Process p = pb.start();
        String out = slurp(p.getInputStream());
        boolean finished = p.waitFor(timeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
        if (!finished) {
            p.destroyForcibly();
            throw new IOException("ffmpeg timed out after " + timeout.getSeconds() + "s\n" + out);
        }
        if (p.exitValue() != 0) {
            throw new IOException("ffmpeg exit " + p.exitValue() + "\n" + out);
        }
    }

    private static String slurp(InputStream is) throws IOException {
        try (is; ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            is.transferTo(bos);
            return bos.toString(java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    private static String normalizeHex(String hex) {
        String h = hex.startsWith("#") ? hex.substring(1) : hex;
        if (!h.matches("(?i)^[0-9a-f]{6}$")) throw new IllegalArgumentException("Bad hex color: " + hex);
        return h;
    }
}
