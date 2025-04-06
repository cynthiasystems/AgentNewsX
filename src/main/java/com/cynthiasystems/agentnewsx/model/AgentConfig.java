package com.cynthiasystems.agentnewsx.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Accessors(fluent = true)
@Builder(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AgentConfig {
  Map<String, Set<String>> urlPatterns;
  Set<String> processedUrls;

  public static AgentConfig of(@NonNull final Map<String, Set<String>> urlPatterns) {
    return AgentConfig.builder().urlPatterns(urlPatterns).processedUrls(new HashSet<>()).build();
  }

  public boolean hasProcessedUrl(@NonNull final String url) {
    return processedUrls.contains(url);
  }

  public List<String> getSearchUrls() {
    return new ArrayList<>(urlPatterns.keySet());
  }

  public boolean patternMatches(@NonNull final String searchUrl, @NonNull final String href) {
    return urlPatterns.get(searchUrl).stream().anyMatch(href::startsWith);
  }
}
