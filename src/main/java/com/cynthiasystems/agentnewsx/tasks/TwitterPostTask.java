package com.cynthiasystems.agentnewsx.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.cynthiasystems.agentflow.tasks.AdaptiveRelayTask;
import com.cynthiasystems.agentflow.tasks.AdaptiveTimingTask;
import com.cynthiasystems.agentnewsx.logging.AgentLog;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.Synchronized;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

@Accessors(fluent = true, chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TwitterPostTask extends AdaptiveTimingTask {

  // Queue of pending tweets
  @Singular List<TweetContent> pendingTweets = new ArrayList<>();

  // Queue of successful tweet results to relay
  @Singular List<AdaptiveRelayTask<TweetResult, ?>> relays = new ArrayList<>();

  // Twitter API client
  Twitter twitterClient;

  // Rate limiting - Twitter has a limit for tweets per day
  // Being conservative with a 45-minute gap between tweets
  @NonFinal long minTimeBetweenTweets = TimeUnit.MINUTES.toMillis(45);
  @NonFinal long lastTweetTime;

  // Maximum retries for failed tweets
  private static final int MAX_RETRIES = 3;

  @Builder
  public TwitterPostTask(
      String apiKey,
      String apiKeySecret,
      String accessToken,
      String accessTokenSecret,
      Long customPostInterval) {
    // Initialize Twitter client with credentials
    ConfigurationBuilder cb = new ConfigurationBuilder();
    cb.setDebugEnabled(true)
        .setOAuthConsumerKey(apiKey)
        .setOAuthConsumerSecret(apiKeySecret)
        .setOAuthAccessToken(accessToken)
        .setOAuthAccessTokenSecret(accessTokenSecret);

    TwitterFactory tf = new TwitterFactory(cb.build());
    this.twitterClient = tf.getInstance();

    if (customPostInterval != null) {
      this.minTimeBetweenTweets = customPostInterval;
    }
  }

  @Override
  @Synchronized
  protected boolean shouldProcess() {
    if (pendingTweets.isEmpty()) {
      return false;
    }

    // Check rate limiting
    long currentTime = System.currentTimeMillis();
    return (currentTime - lastTweetTime) >= minTimeBetweenTweets;
  }

  @Override
  @Synchronized
  protected void process() {
    if (pendingTweets.isEmpty()) {
      return;
    }

    // Get the next tweet to post
    TweetContent tweetContent = pendingTweets.get(0);

    try {
      AgentLog.info("Attempting to post tweet: {}", tweetContent.content());

      // Post to Twitter
      Status status = twitterClient.updateStatus(tweetContent.content());

      // Remove from pending queue
      pendingTweets.remove(0);

      // Update last tweet time for rate limiting
      lastTweetTime = System.currentTimeMillis();

      // Create result with tweet ID and URL
      String tweetUrl = "https://twitter.com/i/web/status/" + status.getId();
      TweetResult result =
          TweetResult.builder()
              .content(tweetContent)
              .tweetId(String.valueOf(status.getId()))
              .tweetUrl(tweetUrl)
              .successful(true)
              .build();

      AgentLog.info("Successfully posted tweet: {}", tweetUrl);

      // Relay the result to next tasks
      for (AdaptiveRelayTask<TweetResult, ?> relay : relays) {
        relay.accept(result);
      }

    } catch (TwitterException e) {
      AgentLog.error("Failed to post tweet: {}", e.getMessage());

      // Handle rate limiting
      if (e.exceededRateLimitation()) {
        AgentLog.warn("Rate limited by Twitter. Will retry later.");
        // Don't remove from queue, will retry
        // Adjust timing based on rate limit reset
        lastTweetTime =
            System.currentTimeMillis()
                - minTimeBetweenTweets
                + (e.getRateLimitStatus() != null
                    ? e.getRateLimitStatus().getSecondsUntilReset() * 1000
                    : TimeUnit.MINUTES.toMillis(15));
      } else {
        // Increment retry count
        tweetContent.incrementRetryCount();

        // If exceeded max retries, remove from queue
        if (tweetContent.retryCount() > MAX_RETRIES) {
          AgentLog.error("Exceeded max retries for tweet. Dropping: {}", tweetContent.content());
          pendingTweets.remove(0);

          // Create failed result
          TweetResult result =
              TweetResult.builder()
                  .content(tweetContent)
                  .successful(false)
                  .errorMessage(e.getMessage())
                  .build();

          // Relay the failed result
          for (AdaptiveRelayTask<TweetResult, ?> relay : relays) {
            relay.accept(result);
          }
        }
      }
    }
  }

  @Synchronized
  public void accept(@NonNull final TweetContent tweetContent) {
    pendingTweets.add(tweetContent);
    AgentLog.info("Added tweet to queue. Queue size: {}", pendingTweets.size());
  }

  @Synchronized
  public void relay(@NonNull final AdaptiveRelayTask<TweetResult, ?> relay) {
    relays.add(relay);
  }

  /** Tweet content model */
  @Getter
  @Builder
  @Accessors(fluent = true, chain = true)
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static class TweetContent {
    // The actual content to post
    final String content;

    // Original article/content that generated this tweet
    final String sourceUrl;

    // For tracking retry attempts
    @NonFinal int retryCount;

    public void incrementRetryCount() {
      retryCount++;
    }
  }

  /** Tweet result model */
  @Getter
  @Builder
  @Accessors(fluent = true, chain = true)
  @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
  public static class TweetResult {
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
}
