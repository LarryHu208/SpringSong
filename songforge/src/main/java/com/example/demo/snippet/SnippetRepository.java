package com.example.demo.snippet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SnippetRepository extends JpaRepository<Snippet, UUID> {
    List<Snippet> findByTitleIgnoreCaseContinaing(String q);
}
