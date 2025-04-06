package com.cynthiasystems.agentnewsx.commands;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import com.cynthiasystems.agentflow.Agent;
import com.cynthiasystems.agentnewsx.model.AgentConfig;
import com.cynthiasystems.agentnewsx.tasks.ContentCollectionTask;
import com.cynthiasystems.agentnewsx.tasks.InterestingTweetTask;
import com.cynthiasystems.agentnewsx.tasks.SearchSourcesTask;
import com.cynthiasystems.agentnewsx.tasks.TwitterPostTask;

import lombok.SneakyThrows;
import lombok.Value;
import lombok.experimental.Accessors;
import picocli.CommandLine;

@Accessors(fluent = true)
@CommandLine.Command(description = "Start the Agent News X agent", name = "StartAgent")
@Value
public class StartAgentCommand implements Callable<Integer> {

  @Override
  @SneakyThrows
  public Integer call() {
    final Map<String, Set<String>> urlPatters =
        Map.of(
            "https://www.pymnts.com/?s=agentic",
                Set.of("https://www.pymnts.com/artificial-intelligence-2/"),
            "https://nvidianews.nvidia.com/", Set.of("https://blogs.nvidia.com/blog/"),
            "https://www.cio.com/search/?q=agentic#gsc.tab=0&gsc.q=agentic&gsc.page=1",
                Set.of("https://www.cio.com/article/"),
            "https://www.forbes.com/search/?q=agentic",
                Set.of("https://www.forbes.com/councils/", "https://www.forbes.com/sites/"));

    final AgentConfig agentConfig = AgentConfig.of(urlPatters);

    final SearchSourcesTask searchSourcesTask = SearchSourcesTask.of();

    final ContentCollectionTask contentCollectionTask = ContentCollectionTask.of();

    final InterestingTweetTask interestingTweetTask = InterestingTweetTask.of();

    final TwitterPostTask twitterPostTask = TwitterPostTask.of();

    searchSourcesTask.relay(contentCollectionTask);

    contentCollectionTask.relay(interestingTweetTask);

    interestingTweetTask.relay(twitterPostTask);

    searchSourcesTask.accept(agentConfig);

    final Agent agent =
        Agent.builder()
            .task(searchSourcesTask)
            .task(contentCollectionTask)
            .task(interestingTweetTask)
            .task(twitterPostTask)
            .build();

    agent.start();

    new CountDownLatch(1).await();

    return 0;
  }
}
