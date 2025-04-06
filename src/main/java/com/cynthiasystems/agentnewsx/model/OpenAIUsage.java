package com.cynthiasystems.agentnewsx.model;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

/** Represents token usage information from the OpenAI model response. */
@Accessors(fluent = true)
@Builder
@Value
public class OpenAIUsage {
  int promptTokens;
  int completionTokens;
  int totalTokens;
}
