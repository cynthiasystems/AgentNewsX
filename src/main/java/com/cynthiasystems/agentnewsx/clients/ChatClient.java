package com.cynthiasystems.agentnewsx.clients;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

import com.cynthiasystems.agentnewsx.logging.AgentLog;
import com.cynthiasystems.agentnewsx.model.IdentifiableContent;

import lombok.NonNull;

public abstract class ChatClient {

  public abstract IdentifiableContent getResponse(
      @NonNull final String modelName,
      @NonNull final String system,
      @NonNull final String prompt,
      final Duration timeout,
      final int maxRetries);

  public <T> CompletableFuture<T> retryAction(
      @NonNull final Function<Integer, CompletableFuture<T>> action,
      final int maxRetries,
      final int retryCount) {
    return action
        .apply(retryCount)
        .exceptionally(
            e -> {
              if (retryCount < maxRetries) {
                AgentLog.info(String.format("Retrying action with count %s", retryCount));
                return retryAction(action, maxRetries, retryCount + 1).join();
              } else {
                throw new CompletionException(new RuntimeException("Exceeded maximum retries", e));
              }
            });
  }
}
