package com.cynthiasystems.agentnewsx.model;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Builder
@Value
public class TweetResult {
  // The original content that was posted
  TweetContent content;

  // Was the tweet successfully posted?
  boolean successful;

  // Twitter's ID for the tweet (only if successful)
  String tweetId;

  // URL to the tweet (only if successful)
  String tweetUrl;

  // Error message if not successful
  String errorMessage;
}
