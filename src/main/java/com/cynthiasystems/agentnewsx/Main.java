package com.cynthiasystems.agentnewsx;

import static com.cynthiasystems.agentnewsx.Constants.BANNER;
import static com.cynthiasystems.agentnewsx.utils.ResourceUtils.readStringResource;

import com.cynthiasystems.agentnewsx.commands.AgentNewsXCommand;
import com.cynthiasystems.agentnewsx.logging.AgentLog;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import picocli.CommandLine;

@UtilityClass
public class Main {
  public static void main(@NonNull final String... args) {
    AgentLog.info(readStringResource(BANNER));
    new CommandLine(AgentNewsXCommand.of()).execute(args);
  }
}
