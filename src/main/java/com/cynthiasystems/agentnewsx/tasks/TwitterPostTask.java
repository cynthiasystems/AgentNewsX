package com.cynthiasystems.agentnewsx.tasks;

import static com.cynthiasystems.agentnewsx.utils.ResourceUtils.readApplicationProperties;

import java.util.Properties;

import com.cynthiasystems.agentflow.tasks.AdaptiveRelayTask;
import com.cynthiasystems.agentnewsx.logging.AgentLog;
import com.cynthiasystems.agentnewsx.model.TweetConfig;
import com.cynthiasystems.agentnewsx.model.TweetContent;
import com.cynthiasystems.agentnewsx.model.TweetResult;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/** A task for posting tweets to Twitter using the Twitter API. */
@Accessors(fluent = true, chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TwitterPostTask extends AdaptiveRelayTask<TweetConfig, TweetResult> {
  /** Private constructor that initializes the task with an expression function. */
  private TwitterPostTask() {
    // Pass the expression function to the superclass constructor
    super(TwitterPostTask::postTweet);
  }

  /**
   * Static factory method to create a TwitterPostTask instance.
   *
   * @return A new TwitterPostTask configured with credentials from application properties
   */
  public static TwitterPostTask of() {
    return new TwitterPostTask();
  }

  /**
   * Static function to post a tweet and generate a result. This is separated from the instance to
   * use as the expression function.
   *
   * @param tweetConfig The content to post
   * @return A TweetResult containing the outcome
   */
  private static TweetResult postTweet(@NonNull final TweetConfig tweetConfig) {

    final Properties properties = readApplicationProperties();
    final String apiKey = properties.getProperty("twitter.api.key");
    final String apiKeySecret = properties.getProperty("twitter.api.key.secret");
    final String accessToken = properties.getProperty("twitter.access.token");
    final String accessTokenSecret = properties.getProperty("twitter.access.token.secret");

    // Initialize Twitter client with credentials
    final ConfigurationBuilder cb = new ConfigurationBuilder();

    cb.setDebugEnabled(true)
        .setOAuthConsumerKey(apiKey)
        .setOAuthConsumerSecret(apiKeySecret)
        .setOAuthAccessToken(accessToken)
        .setOAuthAccessTokenSecret(accessTokenSecret);

    final TwitterFactory factory = new TwitterFactory(cb.build());
    final Twitter twitterClient = factory.getInstance();

    final TweetContent tweetContent = tweetConfig.tweetContent();

    try {
      // Format the tweet content with interestingness score
      final String formattedContent =
          tweetContent.content().trim()
              + "\n\n(Interestingness: "
              + tweetContent.interestingScore()
              + " / 100)";

      AgentLog.info("Posting tweet: {}", formattedContent);

      // Create a StatusUpdate object instead of directly using updateStatus
      final StatusUpdate statusUpdate = new StatusUpdate(formattedContent);

      statusUpdate.setInReplyToStatusId(-1L); // Not a reply

      // Add the URL to the tweet without including it in the text
      statusUpdate.attachmentUrl(tweetContent.sourceUrl());

      // Post to Twitter with the StatusUpdate object
      final Status status = twitterClient.updateStatus(statusUpdate);

      // Create result with tweet ID and URL
      final String tweetUrl = "https://twitter.com/i/web/status/" + status.getId();
      final TweetResult result =
          TweetResult.builder()
              .content(tweetContent)
              .tweetId(String.valueOf(status.getId()))
              .tweetUrl(tweetUrl)
              .successful(true)
              .build();

      AgentLog.info("Successfully posted tweet: {}", tweetUrl);
      return result;

    } catch (final TwitterException e) {
      AgentLog.error("Failed to post tweet: {}", e.getMessage());

      // Create failed result
      return TweetResult.builder()
          .content(tweetContent)
          .successful(false)
          .errorMessage(e.getMessage())
          .build();
    }
  }
}
