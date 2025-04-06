package com.cynthiasystems.agentnewsx.commands;

import java.util.concurrent.Callable;

import lombok.SneakyThrows;
import lombok.Value;
import lombok.experimental.Accessors;
import picocli.CommandLine;

@Accessors(fluent = true)
@CommandLine.Command(description = "Start the TwitterAgent", name = "StartAgent")
@Value
public class StartAgentCommand implements Callable<Integer> {

  @Override
  @SneakyThrows
  public Integer call() {
    return 0;
  }
}
