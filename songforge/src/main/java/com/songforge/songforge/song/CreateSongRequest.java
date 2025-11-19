package com.songforge.songforge.song;

import jakarta.validation.constraints.NotBlank;

public record CreateSongRequest(@NotBlank String prompt, String style) {}
