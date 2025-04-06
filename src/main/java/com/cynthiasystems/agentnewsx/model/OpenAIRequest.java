package com.cynthiasystems.agentnewsx.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Singular;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/** Represents a request to the OpenAI API. */
@Accessors(fluent = true)
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OpenAIRequest {
  String model;

  @JsonProperty("max_tokens")
  Integer maxTokens;

  Boolean stream;
  Double temperature;
  @Singular List<OpenAIMessage> messages;
}
