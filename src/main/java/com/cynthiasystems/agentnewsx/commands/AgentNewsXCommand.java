package com.cynthiasystems.agentnewsx.commands;

import lombok.AccessLevel;
import lombok.Builder;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;

@Builder(access = AccessLevel.PRIVATE)
@Command(
    description = "TwitterAgent",
    mixinStandardHelpOptions = true,
    name = "TwitterAgent",
    subcommands = {HelpCommand.class, StartAgentCommand.class},
    version = "1.0.0")
public class AgentNewsXCommand {
  public static AgentNewsXCommand of() {
    return AgentNewsXCommand.builder().build();
  }
}
