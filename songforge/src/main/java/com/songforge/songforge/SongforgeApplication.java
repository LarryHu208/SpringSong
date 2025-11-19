package com.songforge.songforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SongforgeApplication {

	public static void main(String[] args) {
		SpringApplication.run(SongforgeApplication.class, args);
	}
}