package com.cynthiasystems.agentnewsx.tasks;

import static com.cynthiasystems.agentnewsx.utils.ResourceUtils.readStringResource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cynthiasystems.agentflow.tasks.AdaptiveRelayTask;
import com.cynthiasystems.agentnewsx.clients.LambdaLabsClient;
import com.cynthiasystems.agentnewsx.logging.AgentLog;
import com.cynthiasystems.agentnewsx.model.ArticleContent;
import com.cynthiasystems.agentnewsx.model.IdentifiableContent;
import com.cynthiasystems.agentnewsx.model.InterestingConfig;
import com.cynthiasystems.agentnewsx.model.TweetContent;
import com.cynthiasystems.agentnewsx.utils.JsonUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * Task that evaluates articles for interestingness and converts them to tweet content. Takes
 * InterestingConfig as input and produces TweetContent as output.
 */
@Accessors(fluent = true, chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InterestingTweetTask extends AdaptiveRelayTask<InterestingConfig, TweetContent> {
  // Pattern to extract JSON from response
  private static final Pattern JSON_PATTERN = Pattern.compile("\\{.*\\}", Pattern.DOTALL);

  /** Private constructor that initializes the task with an expression function. */
  @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
  private InterestingTweetTask() {
    super(InterestingTweetTask::processArticles);
  }

  /**
   * Factory method to create a new InterestingTweetTask.
   *
   * @return a new InterestingTweetTask
   */
  public static InterestingTweetTask of() {
    return new InterestingTweetTask();
  }

  /**
   * Processes articles from the InterestingConfig, creates TweetContent for each, sorts by interest
   * score, and returns the most interesting one.
   *
   * @param interestingConfig the config containing articles
   * @return the most interesting TweetContent or null if none meet criteria
   */
  private static TweetContent processArticles(@NonNull final InterestingConfig interestingConfig) {
    final String systemPrompt = readStringResource("/prompts/CreateInterestingTweet");
    final LambdaLabsClient llmClient = LambdaLabsClient.of();

    // Create a list to store all tweet content
    List<TweetContent> allTweets = new ArrayList<>();

    // Process each article to generate tweet content
    for (ArticleContent article : interestingConfig.articleContents()) {
      try {
        AgentLog.info("Processing article for tweet: " + article.title());

        // Prepare the article content as JSON for the model
        String articleJson = JsonUtils.toJson(article);

        // Send to LLM for processing
        IdentifiableContent response =
            llmClient.getResponse(
                "claude-3-5-sonnet-20240229", // Use appropriate model name
                systemPrompt,
                articleJson,
                Duration.ofSeconds(30),
                2 // retries
                );

        if (response != null) {
          // Extract JSON from the response
          String content = response.content();

          // Check for <thinking> tag and extract its content if present
          String thoughts = "";
          if (content.contains("<think>")) {
            int startIndex = content.indexOf("<think>") + "<think>".length();
            int endIndex = content.indexOf("</think>");
            if (endIndex > startIndex) {
              thoughts = content.substring(startIndex, endIndex).trim();
              // Remove the thinking section from the content
              content = content.substring(endIndex + "</think>".length()).trim();
            }
          }

          // Extract JSON from the content
          Matcher jsonMatcher = JSON_PATTERN.matcher(content);
          if (jsonMatcher.find()) {
            String jsonContent = jsonMatcher.group(0);

            // Parse the JSON to create TweetContent
            TweetContent tweetContent = JsonUtils.toObject(jsonContent, TweetContent.class);

            // If thoughts weren't extracted from a thinking tag but are in the JSON, use those
            if (thoughts.isEmpty() && tweetContent.thoughts() != null) {
              thoughts = tweetContent.thoughts();
            }

            // Create a new TweetContent with any extracted thoughts
            if (!thoughts.equals(tweetContent.thoughts())) {
              tweetContent =
                  TweetContent.builder()
                      .thoughts(thoughts)
                      .content(tweetContent.content())
                      .sourceUrl(tweetContent.sourceUrl())
                      .interestingScore(tweetContent.interestingScore())
                      .build();
            }

            AgentLog.info("Created tweet with interest score: " + tweetContent.interestingScore());
            allTweets.add(tweetContent);
          } else {
            AgentLog.warn("Could not extract JSON from LLM response");
          }
        }

      } catch (Exception e) {
        AgentLog.error("Error processing article for tweet: " + e.getMessage());
      }
    }

    // Sort tweets by interestingness score (descending)
    allTweets.sort(Comparator.comparing(TweetContent::interestingScore).reversed());

    // Return the most interesting tweet, or null if none were found
    if (!allTweets.isEmpty()) {
      TweetContent bestTweet = allTweets.get(0);
      AgentLog.info("Selected most interesting tweet with score: " + bestTweet.interestingScore());
      return bestTweet;
    } else {
      AgentLog.info("No interesting tweets found from any articles");
      return null;
    }
  }
}
