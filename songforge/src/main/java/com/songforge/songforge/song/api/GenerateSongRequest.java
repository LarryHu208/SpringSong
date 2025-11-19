package com.songforge.songforge.song.api;

import jakarta.validation.constraints.NotBlank;

public record GenerateSongRequest(
        @NotBlank String prompt,
        @NotBlank String style
) {}
