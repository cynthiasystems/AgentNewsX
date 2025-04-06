package com.cynthiasystems.agentnewsx.tasks;

import static com.cynthiasystems.agentnewsx.utils.ResourceUtils.readStringResource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cynthiasystems.agentflow.tasks.AdaptiveRelayTask;
import com.cynthiasystems.agentnewsx.clients.LambdaLabsClient;
import com.cynthiasystems.agentnewsx.logging.AgentLog;
import com.cynthiasystems.agentnewsx.model.ArticleContent;
import com.cynthiasystems.agentnewsx.model.IdentifiableContent;
import com.cynthiasystems.agentnewsx.model.InterestingConfig;
import com.cynthiasystems.agentnewsx.model.TweetConfig;
import com.cynthiasystems.agentnewsx.model.TweetContent;
import com.cynthiasystems.agentnewsx.utils.JsonUtils;

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
public class InterestingTweetTask extends AdaptiveRelayTask<InterestingConfig, TweetConfig> {
  // Pattern to extract JSON from response
  private static final Pattern JSON_PATTERN = Pattern.compile("\\{.*\\}", Pattern.DOTALL);

  /** Private constructor that initializes the task with an expression function. */
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
   * @param config the config containing articles
   * @return the most interesting TweetContent or null if none meet criteria
   */
  private static TweetConfig processArticles(@NonNull final InterestingConfig config) {
    final String systemPrompt = readStringResource("/prompts/CreateInterestingTweet");

    final LambdaLabsClient llmClient = LambdaLabsClient.of();

    // Create a list to store all tweet content
    final List<TweetContent> allTweets = new ArrayList<>();

    // Process each article to generate tweet content
    for (final ArticleContent article : config.articleContents()) {
      try {
        AgentLog.info("Processing article for tweet: " + article.title());

        // Prepare the article content as JSON for the model
        final String articleJson = JsonUtils.toJson(article);

        // Send to LLM for processing
        final IdentifiableContent response =
            llmClient.getResponse(
                "deepseek-r1-671b", // Use appropriate model name
                systemPrompt,
                articleJson,
                Duration.ofSeconds(60),
                2);

        if (Optional.ofNullable(response).isPresent()) {
          // Extract JSON from the response
          final String content = response.content().replaceAll("<think>[\\s\\S]*?</think>", "");

          // Extract JSON from the content
          final Matcher jsonMatcher = JSON_PATTERN.matcher(content);
          if (jsonMatcher.find()) {
            final String jsonContent = jsonMatcher.group(0);

            // Parse the JSON to create TweetContent
            final TweetContent tweetContent = JsonUtils.toObject(jsonContent, TweetContent.class);

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
      final TweetContent bestTweet = allTweets.get(0);
      AgentLog.info("Selected most interesting tweet with score: " + bestTweet.interestingScore());
      return TweetConfig.builder().interestingConfig(config).tweetContent(bestTweet).build();
    } else {
      AgentLog.info("No interesting tweets found from any articles");
      return null;
    }
  }
}
