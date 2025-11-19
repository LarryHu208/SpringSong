INSERT INTO song_job (id, prompt, style, status, audio_format, audio_path)
VALUES
  (uuid_generate_v4(), 'lofi about code', 'lofi', 'READY', 'wav', 'songs/demo.wav'),
  (uuid_generate_v4(), 'ambient focus',   'ambient', 'QUEUED', NULL, NULL);
