package com.cynthiasystems.agentnewsx.tasks;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cynthiasystems.agentflow.tasks.AdaptiveRelayTask;
import com.cynthiasystems.agentnewsx.logging.AgentLog;
import com.cynthiasystems.agentnewsx.model.ArticleContent;
import com.cynthiasystems.agentnewsx.model.ContentConfig;
import com.cynthiasystems.agentnewsx.model.InterestingConfig;
import com.cynthiasystems.agentnewsx.web.DynamicWebCrawler;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * A task that processes URLs and extracts article content. This task accepts a URL string and
 * produces ArticleContent.
 */
@Accessors(fluent = true, chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContentCollectionTask extends AdaptiveRelayTask<ContentConfig, InterestingConfig> {
  private static final int URL_PARTS_LENGTH = 2;

  // Set of already processed URLs to avoid duplicates
  Set<String> processedUrls = ConcurrentHashMap.newKeySet();

  /** Private constructor that initializes the task with an expression function. */
  private ContentCollectionTask() {
    super(ContentCollectionTask::processUrls);
  }

  public static ContentCollectionTask of() {
    return new ContentCollectionTask();
  }

  /**
   * Processes a URL and extracts article content. Static method used as the expression function.
   *
   * @return The extracted article content
   */
  private static InterestingConfig processUrls(@NonNull final ContentConfig contentConfig) {
    final List<String> urls = contentConfig.urls();
    final List<ArticleContent> articleContents = new ArrayList<>();
    for (final String url : urls) {
      try {
        AgentLog.info("Processing article: " + url);

        // Download the page content
        final String content = DynamicWebCrawler.getRenderedHtml(url);
        if (content == null || content.isEmpty()) {
          AgentLog.warn("Empty content for URL: " + url);
          continue;
        }

        // Parse the HTML
        final Document doc = Jsoup.parse(content);

        // Extract article information
        final String title = extractTitle(doc);
        final String description = extractDescription(doc);
        final String mainContent = extractMainContent(doc);
        final String source = extractSource(url);
        final ZonedDateTime publishDate = extractPublishDate(doc);

        // Create article content object
        ArticleContent articleContent =
            ArticleContent.builder()
                .url(url)
                .title(title)
                .description(description)
                .content(mainContent)
                .source(source)
                .publishDate(publishDate)
                .build();

        AgentLog.info("Processed article: " + title);
        articleContents.add(articleContent);

      } catch (Exception e) {
        AgentLog.error("Error processing URL: " + url + " - " + e.getMessage());
      }
    }
    return InterestingConfig.builder()
        .config(contentConfig)
        .articleContents(articleContents)
        .build();
  }

  /**
   * Extract the title from an HTML document.
   *
   * @param doc The Jsoup document
   * @return The title text
   */
  private static String extractTitle(Document doc) {
    // Try article heading first
    Element heading = doc.selectFirst("article h1, .article-title, .entry-title, .post-title");
    if (heading != null) {
      return heading.text().trim();
    }

    // Fall back to page title
    return doc.title().trim();
  }

  /**
   * Extract the description from an HTML document.
   *
   * @param doc The Jsoup document
   * @return The description text
   */
  private static String extractDescription(Document doc) {
    // Try meta description
    Element meta = doc.selectFirst("meta[name=description]");
    if (meta != null) {
      return meta.attr("content").trim();
    }

    // Try article summary/subtitle
    Element summary = doc.selectFirst(".article-summary, .entry-summary, .post-summary, .subtitle");
    if (summary != null) {
      return summary.text().trim();
    }

    // Try first paragraph
    Element firstP = doc.selectFirst("article p, .article-content p, .entry-content p");
    if (firstP != null) {
      return firstP.text().trim();
    }

    return "";
  }

  /**
   * Extract the main content from an HTML document.
   *
   * @param doc The Jsoup document
   * @return The main content text
   */
  private static String extractMainContent(Document doc) {
    // Try common article content selectors
    Element content =
        doc.selectFirst("article, .article-content, .entry-content, .post-content, main");

    if (content != null) {
      // Remove non-content elements
      content.select("aside, nav, footer, .comments, .related, .share, script, style").remove();

      // Get all paragraphs
      Elements paragraphs = content.select("p");
      StringBuilder sb = new StringBuilder();

      for (Element p : paragraphs) {
        String text = p.text().trim();
        if (!text.isEmpty()) {
          sb.append(text).append("\n\n");
        }
      }

      return sb.toString().trim();
    }

    // Fall back to all paragraphs
    Elements paragraphs = doc.select("p");
    StringBuilder sb = new StringBuilder();

    for (Element p : paragraphs) {
      String text = p.text().trim();
      if (!text.isEmpty()) {
        sb.append(text).append("\n\n");
      }
    }

    return sb.toString().trim();
  }

  /**
   * Extract the source from a URL.
   *
   * @param url The article URL
   * @return The source name
   */
  private static String extractSource(String url) {
    try {
      URI uri = URI.create(url);
      String host = uri.getHost();

      // Remove www. prefix
      if (host.startsWith("www.")) {
        host = host.substring(4);
      }

      // Get domain name
      String[] parts = host.split("\\.");
      if (parts.length >= URL_PARTS_LENGTH) {
        return parts[parts.length - 2];
      }

      return host;
    } catch (Exception e) {
      return "";
    }
  }

  /**
   * Extract the publication date from an HTML document.
   *
   * @param doc The Jsoup document
   * @return The publication date or null if not found
   */
  private static ZonedDateTime extractPublishDate(Document doc) {
    try {
      // Try meta tags
      Element metaDate = doc.selectFirst("meta[property=article:published_time]");
      if (metaDate != null) {
        return ZonedDateTime.parse(metaDate.attr("content"));
      }

      // Try time elements
      Element timeElement = doc.selectFirst("time");
      if (timeElement != null && timeElement.hasAttr("datetime")) {
        return ZonedDateTime.parse(timeElement.attr("datetime"));
      }

      // Try common date selectors
      Element dateElement = doc.selectFirst(".date, .published, .post-date, .article-date");
      if (dateElement != null) {
        // This would require more complex date parsing logic
        // For now, return current time as fallback
      }

    } catch (Exception e) {
      AgentLog.error("Error downloading published date: " + e.getMessage());
      // Parsing errors, just return null
    }

    return null;
  }
}
