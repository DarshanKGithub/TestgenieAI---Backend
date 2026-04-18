package com.testgenieai.backend.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PlaywrightRunnerService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${playwright.worker.dir}")
    private String workerDir;

    public PlaywrightRunOutput execute(PlaywrightRunInput input) {
        Path tempInput = null;
        Path tempOutput = null;
        try {
            tempInput = Files.createTempFile("testgenie-playwright-input-", ".json");
            tempOutput = Files.createTempFile("testgenie-playwright-output-", ".json");
            objectMapper.writeValue(tempInput.toFile(), input);

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "node",
                    "run-tests.mjs",
                    tempInput.toAbsolutePath().toString(),
                    tempOutput.toAbsolutePath().toString()
            );
            processBuilder.directory(Path.of(workerDir).toFile());
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                String output = new String(process.getInputStream().readAllBytes());
                throw new IllegalStateException("Playwright execution failed: " + output);
            }

            return objectMapper.readValue(tempOutput.toFile(), PlaywrightRunOutput.class);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to execute Playwright runner", ex);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to execute Playwright runner", ex);
        } finally {
            tryDelete(tempInput);
            tryDelete(tempOutput);
        }
    }

    private void tryDelete(Path file) {
        if (file == null) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
        }
    }
}
