package com.example.demo.snippet;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class SnippetService {
    private final SnippetRepository repo;
    public SnippetService(SnippetRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public Snippet create(String title, String code) {
        if (title == null || title.isBlank() || title.length() > 80) {
            throw new IllegalArgumentException("title must be non-empty and <= 80 chars");
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code must be non-empty");
        }
        var s = new Snippet();
        s.setTitle(title.trim());
        s.setCode(code);
        return repo.save(s);
    }

    @Transactional(readOnly = true)
    public Snippet get(UUID id) {
        return repo.findById(id).orElseThrow(() ->
                new NoSuchElementException("Snippet not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Snippet> search(String q) {
        return (q == null || q.isBlank()) ? repo.findAll()
                : repo.findByTitleIgnoreCaseContinaing(q.trim());
    }
}
