package com.songforge.songforge.storage;

import com.songforge.songforge.config.StorageProps;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Service
public class FileSystemStorageService {

    private final Path base;

    public FileSystemStorageService(StorageProps props) throws IOException {
        this.base = Path.of(props.getBaseDir() == null ? "storage" : props.getBaseDir());
        Files.createDirectories(this.base);
    }

    public Path writeBytes(String relative, byte[] bytes) throws IOException {
        Path p = base.resolve(relative).normalize();
        Files.createDirectories(p.getParent());
        Files.write(p, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return p;
    }

    public Path resolve(String relative) {
        return base.resolve(relative).normalize();
    }
}
