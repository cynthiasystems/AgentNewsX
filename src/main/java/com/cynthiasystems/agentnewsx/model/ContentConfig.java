package com.cynthiasystems.agentnewsx.model;

import java.util.Collections;
import java.util.List;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Accessors(fluent = true)
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContentConfig {
  AgentConfig config;

  @Getter(AccessLevel.NONE)
  @Singular
  List<String> urls;

  public List<String> urls() {
    return Collections.unmodifiableList(urls);
  }
}
