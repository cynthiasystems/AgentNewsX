package com.cynthiasystems.agentnewsx.model;

import java.time.Instant;
import java.time.ZonedDateTime;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

/** Represents the parsed content of an article. */
@Accessors(fluent = true)
@Builder
@Value
public class ArticleContent {
  /** The URL of the article. */
  String url;

  /** The title of the article. */
  String title;

  /** A brief description or summary of the article. */
  String description;

  /** The main content of the article. */
  String content;

  /** The source website or publisher. */
  String source;

  /** The publication date if available. */
  ZonedDateTime publishDate;

  /** The time when the article was collected. */
  Instant collectedAt = Instant.now();
}
