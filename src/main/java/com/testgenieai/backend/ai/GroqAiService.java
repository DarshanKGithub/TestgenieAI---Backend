package com.testgenieai.backend.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testgenieai.backend.dto.FailureAnalysisRequest;
import com.testgenieai.backend.dto.FailureAnalysisResponse;
import com.testgenieai.backend.dto.GenerateTestCasesRequest;
import com.testgenieai.backend.dto.GenerateTestCasesResponse;
import com.testgenieai.backend.dto.TestCaseResultRequest;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GroqAiService {

    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ai.groq.api-key:}")
    private String apiKey;

    @Value("${ai.groq.base-url:https://api.groq.com/openai}")
    private String baseUrl;

    @Value("${ai.groq.model:llama-3.3-70b-versatile}")
    private String model;

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String currentMode() {
        return isConfigured() ? "AI enabled" : "Fallback mode";
    }

    public String currentModel() {
        return model;
    }

    public String currentBaseUrl() {
        return baseUrl;
    }

    public Optional<GenerateTestCasesResponse> generateTestCases(GenerateTestCasesRequest request) {
        if (!isConfigured()) {
            return Optional.empty();
        }

        String prompt = """
                You are a senior QA automation engineer.
                Generate executable website test cases in JSON format only.
                Output strictly this schema:
                {
                  "suiteName": "string",
                  "targetUrl": "string",
                  "testCases": [
                    {
                      "testName": "string",
                      "path": "string",
                      "expectedText": "string or null"
                    }
                  ]
                }
                Requirements:
                - Include functional, edge, and negative cases.
                - Keep path relative (start with /).
                - Keep test names concise and practical.
                Target URL: %s
                User Flow:
                %s
                """.formatted(request.targetUrl(), request.userFlow());

        String responseText = callGroq(prompt);
        if (responseText == null || responseText.isBlank()) {
            return Optional.empty();
        }

        try {
            JsonNode root = objectMapper.readTree(responseText);
            JsonNode suiteNameNode = root.get("suiteName");
            JsonNode targetUrlNode = root.get("targetUrl");
            JsonNode testCasesNode = root.get("testCases");

            if (suiteNameNode == null || targetUrlNode == null || testCasesNode == null || !testCasesNode.isArray()) {
                return Optional.empty();
            }

            List<TestCaseResultRequest> testCases = new ArrayList<>();
            for (JsonNode item : testCasesNode) {
                String testName = asText(item.get("testName"));
                String path = normalizePath(asText(item.get("path")));
                String expectedText = nullableText(item.get("expectedText"));
                if (!testName.isBlank() && !path.isBlank()) {
                    testCases.add(new TestCaseResultRequest(testName, path, expectedText));
                }
            }

            if (testCases.isEmpty()) {
                return Optional.empty();
            }

            String suiteName = asText(suiteNameNode);
            String targetUrl = asText(targetUrlNode);
            if (suiteName.isBlank()) {
                suiteName = "AI Generated Suite";
            }
            if (targetUrl.isBlank()) {
                targetUrl = request.targetUrl();
            }

            return Optional.of(new GenerateTestCasesResponse(suiteName, targetUrl, testCases));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    public Optional<FailureAnalysisResponse> analyzeFailure(FailureAnalysisRequest request) {
        if (!isConfigured()) {
            return Optional.empty();
        }

        String prompt = """
                You are a software testing root-cause analysis assistant.
                Analyze the provided test failure logs and return strict JSON only.
                Output schema:
                {
                  "summary": "string",
                  "probableCauses": ["string"],
                  "suggestedFixes": ["string"]
                }
                Suite: %s
                Test: %s
                Logs:
                %s
                """.formatted(
                request.suiteName() == null ? "N/A" : request.suiteName(),
                request.testName() == null ? "N/A" : request.testName(),
                request.errorLog()
        );

        String responseText = callGroq(prompt);
        if (responseText == null || responseText.isBlank()) {
            return Optional.empty();
        }

        try {
            JsonNode root = objectMapper.readTree(responseText);
            String summary = asText(root.get("summary"));
            List<String> causes = asTextList(root.get("probableCauses"));
            List<String> fixes = asTextList(root.get("suggestedFixes"));

            if (summary.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(new FailureAnalysisResponse(summary, causes, fixes));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private String callGroq(String prompt) {
        try {
            String body = objectMapper.writeValueAsString(new ChatRequest(
                    model,
                    List.of(
                            new ChatMessage("system", "Return only valid JSON. No markdown. No explanation outside JSON."),
                            new ChatMessage("user", prompt)
                    ),
                    0.2
            ));

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v1/chat/completions"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return null;
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            if (contentNode.isMissingNode()) {
                return null;
            }

            String content = contentNode.asText("").trim();
            return unwrapCodeFence(content);
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return null;
        }
    }

    private String unwrapCodeFence(String content) {
        if (content.startsWith("```")) {
            int firstNewline = content.indexOf('\n');
            int lastFence = content.lastIndexOf("```");
            if (firstNewline > -1 && lastFence > firstNewline) {
                return content.substring(firstNewline + 1, lastFence).trim();
            }
        }
        return content;
    }

    private String asText(JsonNode node) {
        if (node == null || node.isNull()) {
            return "";
        }
        return node.asText("").trim();
    }

    private String nullableText(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String value = node.asText("").trim();
        return value.isBlank() ? null : value;
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }
        String clean = path.trim();
        if (clean.toLowerCase(Locale.ROOT).startsWith("http://") || clean.toLowerCase(Locale.ROOT).startsWith("https://")) {
            return "/";
        }
        return clean.startsWith("/") ? clean : "/" + clean;
    }

    private List<String> asTextList(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        List<String> items = new ArrayList<>();
        for (JsonNode item : node) {
            String text = asText(item);
            if (!text.isBlank()) {
                items.add(text);
            }
        }
        return items;
    }

    private record ChatRequest(String model, List<ChatMessage> messages, double temperature) {
    }

    private record ChatMessage(String role, String content) {
    }
}
