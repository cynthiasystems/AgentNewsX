package com.cynthiasystems.agentnewsx.model;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Builder
@Value
public class TweetConfig {
  InterestingConfig interestingConfig;
  TweetContent tweetContent;
}
