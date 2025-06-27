package com.workpilot.configuration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileStorageConfig {

    @Bean
    CommandLineRunner init() {
        return (args) -> {
            // Créer le dossier uploads s'il n'existe pas
            Path uploadsDir = Paths.get("uploads");
            if (!Files.exists(uploadsDir)) {
                Files.createDirectories(uploadsDir);
                System.out.println("Created uploads directory: " + uploadsDir.toAbsolutePath());
            } else {
                System.out.println("Uploads directory already exists: " + uploadsDir.toAbsolutePath());
            }
        };
    }
} 