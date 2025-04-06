package com.cynthiasystems.agentnewsx.tasks;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
  private static InterestingConfig processUrls(@NonNull final ContentConfig config) {
    final List<String> urls = config.urls();
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
        final ArticleContent articleContent =
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
    return InterestingConfig.builder().config(config).articleContents(articleContents).build();
  }

  /**
   * Extract the title from an HTML document.
   *
   * @param document The Jsoup document
   * @return The title text
   */
  private static String extractTitle(@NonNull final Document document) {
    // Try article heading first
    final Element heading =
        document.selectFirst("article h1, .article-title, .entry-title, .post-title");
    if (Optional.ofNullable(heading).isPresent()) {
      return heading.text().trim();
    }

    // Fall back to page title
    return document.title().trim();
  }

  /**
   * Extract the description from an HTML document.
   *
   * @param document The Jsoup document
   * @return The description text
   */
  private static String extractDescription(@NonNull final Document document) {
    // Try meta description
    final Element meta = document.selectFirst("meta[name=description]");
    if (Optional.ofNullable(meta).isPresent()) {
      return meta.attr("content").trim();
    }

    // Try article summary/subtitle
    final Element summary =
        document.selectFirst(".article-summary, .entry-summary, .post-summary, .subtitle");
    if (Optional.ofNullable(summary).isPresent()) {
      return summary.text().trim();
    }

    // Try first paragraph
    final Element firstParagraph =
        document.selectFirst("article p, .article-content p, .entry-content p");
    if (Optional.ofNullable(firstParagraph).isPresent()) {
      return firstParagraph.text().trim();
    }

    return "";
  }

  /**
   * Extract the main content from an HTML document.
   *
   * @param document The Jsoup document
   * @return The main content text
   */
  private static String extractMainContent(@NonNull final Document document) {
    // Try common article content selectors
    final Element content =
        document.selectFirst("article, .article-content, .entry-content, .post-content, main");

    final Elements paragraphs;
    if (Optional.ofNullable(content).isPresent()) {
      // Remove non-content elements
      content.select("aside, nav, footer, .comments, .related, .share, script, style").remove();

      // Get all paragraphs
      paragraphs = content.select("p");
    } else {
      // Fall back to all paragraphs
      paragraphs = document.select("p");
    }

    final StringBuilder stringBuilder = new StringBuilder();

    for (final Element paragraph : paragraphs) {
      final String text = paragraph.text().trim();
      if (!text.isEmpty()) {
        stringBuilder.append(text).append("\n\n");
      }
    }

    return stringBuilder.toString().trim();
  }

  /**
   * Extract the source from a URL.
   *
   * @param url The article URL
   * @return The source name
   */
  private static String extractSource(@NonNull final String url) {
    try {
      final URI uri = URI.create(url);
      final String host =
          uri.getHost().startsWith("www.") ? uri.getHost().substring(4) : uri.getHost();

      // Get domain name
      final String[] parts = host.split("\\.");
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
   * @param document The Jsoup document
   * @return The publication date or null if not found
   */
  private static ZonedDateTime extractPublishDate(@NonNull final Document document) {
    try {
      // Try meta tags
      final Element metaDate = document.selectFirst("meta[property=article:published_time]");
      if (Optional.ofNullable(metaDate).isPresent()) {
        return ZonedDateTime.parse(metaDate.attr("content"));
      }

      // Try time elements
      final Element timeElement = document.selectFirst("time");
      if (Optional.ofNullable(timeElement).isPresent() && timeElement.hasAttr("datetime")) {
        return ZonedDateTime.parse(timeElement.attr("datetime"));
      }

      // Try common date selectors
      final Element dateElement =
          document.selectFirst(".date, .published, .post-date, .article-date");
      if (Optional.ofNullable(dateElement).isPresent()) {
        // This would require more complex date parsing logic
        return ZonedDateTime.parse(dateElement.text());
      }

    } catch (final Exception e) {
      AgentLog.error("Error downloading published date: " + e.getMessage());
      // Parsing errors, just return null
    }

    return null;
  }
}
