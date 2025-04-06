package com.cynthiasystems.agentnewsx.model;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Builder
@Value
public class TweetContent {
  String thoughts;
  String content;
  String sourceUrl;
  int interestingScore;
}
