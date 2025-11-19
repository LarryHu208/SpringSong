CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS song_job (
  id            uuid PRIMARY KEY,
  prompt        text NOT NULL,
  style         varchar(64) NOT NULL,
  status        varchar(16) NOT NULL,
  audio_format  varchar(16),
  audio_path    text,
  cover_path    text,
  video_path    text,
  error         text,
  created_at    timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_song_job_status_created
  ON song_job(status, created_at DESC);
