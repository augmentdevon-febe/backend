package com.example.worldcuppredictor.infrastructure.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

// This class is responsible for loading environment variables from a .env file and adding them to the Spring Environment.
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {
    private static final String PROPERTY_SOURCE_NAME = "dotenvProperties";
    private static final Path DOTENV_PATH = Path.of(".env");

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!Files.exists(DOTENV_PATH) || !Files.isReadable(DOTENV_PATH)) {
            return;
        }

        Map<String, Object> properties = new HashMap<>();
        try (Stream<String> lines = Files.lines(DOTENV_PATH)) {
            lines.map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .filter(line -> !line.startsWith("#"))
                    .forEach(line -> {
                        int idx = line.indexOf('=');
                        if (idx <= 0) {
                            return;
                        }
                        String key = line.substring(0, idx).trim();
                        String value = line.substring(idx + 1).trim();
                        if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                            value = value.substring(1, value.length() - 1);
                        }
                        properties.put(key, value);
                    });
        } catch (IOException ignored) {
        }

        if (!properties.isEmpty()) {
            environment.getPropertySources().addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
        }
    }
}
