package com.example.demo.snippet;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController@RequestMapping("/api/snippets")
public class SnippetController {
    private record CreateReq(String title, String code){}
    private final SnippetService svc;
    public SnippetController(SnippetService svc){
        this.svc = svc;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Snippet create(@RequestBody CreateReq req) {
        return svc.create(req.title(), req.code());
    }

    @GetMapping("/{id}")
    public Snippet get(@PathVariable UUID id) {
        return svc.get(id);
    }

    @GetMapping
    public List<Snippet> list(@RequestParam(required = false) String query) {
        return svc.search(query);
    }
}
