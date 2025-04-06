package com.cynthiasystems.agentnewsx.model;

import java.util.Collections;
import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Builder
@Value
@SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
public class InterestingConfig {
  ContentConfig config;

  @Getter(AccessLevel.NONE)
  @Singular
  List<ArticleContent> articleContents;

  public List<ArticleContent> articleContents() {
    return Collections.unmodifiableList(articleContents);
  }
}
