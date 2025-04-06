package com.cynthiasystems.agentnewsx.model;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

/** Represents a single choice/completion returned by the OpenAI model. */
@Accessors(fluent = true)
@Builder
@Value
public class OpenAIChoice {
  int index;
  OpenAIMessage message;
  String finishReason;
}
