package com.cynthiasystems.agentnewsx.tasks;

import static com.cynthiasystems.agentnewsx.utils.URLUtils.resolveUrl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cynthiasystems.agentflow.tasks.AdaptiveRelayTask;
import com.cynthiasystems.agentnewsx.logging.AgentLog;
import com.cynthiasystems.agentnewsx.model.AgentConfig;
import com.cynthiasystems.agentnewsx.model.ContentConfig;
import com.cynthiasystems.agentnewsx.web.DynamicWebCrawler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * A task that crawls search pages to discover article URLs based on matching patterns. Input:
 * Search URL string Output: Article URL string
 */
@Accessors(fluent = true, chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SearchSourcesTask extends AdaptiveRelayTask<AgentConfig, ContentConfig> {
  /** Private constructor that initializes the task with an expression function. */
  @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
  private SearchSourcesTask() {
    super(SearchSourcesTask::crawlSearchPage);
  }

  public static SearchSourcesTask of() {
    return new SearchSourcesTask();
  }

  /**
   * Crawls a search page to find article links matching the patterns. This is the expression
   * function for the AdaptiveRelayTask.
   *
   * @return List of discovered article URLs
   */
  @SneakyThrows
  @Synchronized
  private static ContentConfig crawlSearchPage(@NonNull final AgentConfig agentConfig) {
    final Set<String> discoveredUrls = new HashSet<>();
    for (final String searchUrl : agentConfig.getSearchUrls()) {
      try {
        AgentLog.info("Crawling search page: " + searchUrl);
        final String renderedHtml = DynamicWebCrawler.getRenderedHtml(searchUrl);
        if (Optional.ofNullable(renderedHtml).isEmpty()) {
          continue;
        }
        final Document doc = Jsoup.parse(renderedHtml);
        final Elements links = doc.select("a[href]");
        for (final Element link : links) {
          final String href = link.attr("href");
          final String absoluteHref = resolveUrl(searchUrl, href);
          if (Optional.ofNullable(absoluteHref).isEmpty()) {
            continue;
          }
          final boolean matchesPattern = agentConfig.patternMatches(searchUrl, absoluteHref);
          if (matchesPattern && !agentConfig.hasProcessedUrl(absoluteHref)) {
            if (absoluteHref.toLowerCase(Locale.ROOT).contains("agent")) {
              AgentLog.info("Found article link: " + href);
              discoveredUrls.add(absoluteHref);
            }
          }
        }
      } catch (final Exception e) {
        AgentLog.error("Error while crawling search page: " + searchUrl, e);
      }
    }
    return ContentConfig.builder()
        .urls(new ArrayList<>(discoveredUrls))
        .config(agentConfig)
        .build();
  }
}
