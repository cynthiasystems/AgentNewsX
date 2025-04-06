package com.cynthiasystems.agentnewsx.clients;

import static com.cynthiasystems.agentnewsx.utils.JsonUtils.toJson;
import static com.cynthiasystems.agentnewsx.utils.JsonUtils.toObject;
import static com.cynthiasystems.agentnewsx.utils.ResourceUtils.readApplicationProperties;
import static twitter4j.HttpResponseCode.OK;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import com.cynthiasystems.agentnewsx.logging.AgentLog;
import com.cynthiasystems.agentnewsx.model.IdentifiableContent;
import com.cynthiasystems.agentnewsx.model.OpenAIChoice;
import com.cynthiasystems.agentnewsx.model.OpenAIMessage;
import com.cynthiasystems.agentnewsx.model.OpenAIRequest;
import com.cynthiasystems.agentnewsx.model.OpenAIResponse;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.experimental.Accessors;

/** Lambda Inference API Client that maintains compatibility with EvaClient interface */
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@Builder
@Value
public class LambdaLabsClient extends ChatClient {
  HttpClient client;
  String baseUrl;
  String apiKey;

  public static LambdaLabsClient of() {
    final Properties properties = readApplicationProperties();
    final String baseUrl = properties.getProperty("lamabdalabs.api.url");
    final String apiKey = properties.getProperty("lamabdalabs.api.key");
    final HttpClient client =
        HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofMillis(1500))
            .build();
    return LambdaLabsClient.builder().baseUrl(baseUrl).client(client).apiKey(apiKey).build();
  }

  /** Get a non-streaming response from the API with timeout and retry capabilities */
  @Override
  @SneakyThrows
  public IdentifiableContent getResponse(
      @NonNull final String modelName,
      @NonNull final String system,
      @NonNull final String prompt,
      final Duration timeout,
      final int maxRetries) {
    int attempts = 0;
    while (attempts <= maxRetries) {
      try {
        AgentLog.info(String.format("Attempt %d of %d", attempts + 1, maxRetries + 1));
        final HttpRequest httpRequest =
            HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + "/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey())
                .timeout(timeout)
                .POST(
                    HttpRequest.BodyPublishers.ofString(
                        toJson(createChatCompletionRequest(modelName, system, prompt, false))))
                .build();
        final HttpResponse<String> response =
            client().send(httpRequest, HttpResponse.BodyHandlers.ofString());
        final int status = response.statusCode();
        if (status == OK) {
          final OpenAIResponse openAIResponse = toObject(response.body(), OpenAIResponse.class);
          final OpenAIChoice openAIChoice = openAIResponse.getChoice(0);
          final OpenAIMessage openAIMessage = openAIChoice.message();
          final String content = openAIMessage.content();
          return IdentifiableContent.builder()
              .id(UUID.randomUUID().toString())
              .content(content)
              .build();
        }
        return IdentifiableContent.builder()
            .id(UUID.randomUUID().toString())
            .content(response.body())
            .build();
      } catch (Exception e) {
        AgentLog.error(
            String.format(
                "Request failed (attempt %d of %d). %s",
                attempts + 1, maxRetries + 1, e.getMessage()));
        if (attempts < maxRetries) {
          attempts++;
        } else {
          break;
        }
      }
    }
    throw new RuntimeException("All retry attempts failed");
  }

  /** Helper method to create the chat completion request */
  @SneakyThrows
  public static OpenAIRequest createChatCompletionRequest(
      @NonNull final String modelName,
      @NonNull final String prompt,
      @NonNull final String system,
      final boolean streaming) {
    return OpenAIRequest.builder().model(modelName).maxTokens(2000).stream(streaming)
        .temperature(0.7)
        .messages(
            List.of(
                OpenAIMessage.builder().role("system").content(system).build(),
                OpenAIMessage.builder().role("user").content(prompt).build()))
        .build();
  }
}
