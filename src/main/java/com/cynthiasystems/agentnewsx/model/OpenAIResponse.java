package com.cynthiasystems.agentnewsx.model;

import java.util.List;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Singular;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/** Represents the top-level response from an OpenAI model API. */
@Accessors(fluent = true)
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OpenAIResponse {
  String id;
  String object;
  long created;
  String model;
  @Singular List<OpenAIChoice> choices;
  OpenAIUsage usage;

  public OpenAIChoice getChoice(final int index) {
    return choices.get(index);
  }
}
