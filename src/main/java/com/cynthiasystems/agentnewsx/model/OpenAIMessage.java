package com.cynthiasystems.agentnewsx.model;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

/** Represents a message within a choice from the OpenAI model response. */
@Accessors(fluent = true)
@Builder
@Value
public class OpenAIMessage {
  String role;
  String content;
}
