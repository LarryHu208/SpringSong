package com.songforge.songforge.song;

public record SongJobDto(
        String id, String status, String audioPath, String audioFormat, String error) {

    static SongJobDto from(SongJob j) {
        return new SongJobDto(
                j.getId().toString(),
                j.getStatus(),
                j.getAudioPath(),
                j.getAudioFormat(),
                j.getError());
    }
}
