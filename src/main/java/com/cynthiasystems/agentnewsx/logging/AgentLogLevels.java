package com.cynthiasystems.agentnewsx.logging;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

// Log levels
@Accessors(fluent = true)
@Getter
public enum AgentLogLevels {
  TRACE(0, "TRACE", "\u001B[90m"), // Gray
  DEBUG(1, "DEBUG", "\u001B[36m"), // Cyan
  INFO(2, "INFO", "\u001B[32m"), // Green
  WARN(3, "WARN", "\u001B[33m"), // Yellow
  ERROR(4, "ERROR", "\u001B[31m"), // Red
  FATAL(5, "FATAL", "\u001B[35m"); // Magenta

  private static final AgentLogLevels globalLevel = AgentLogLevels.INFO;

  final int value;
  final String level;
  final String ansiColor;

  AgentLogLevels(final int value, @NonNull final String level, @NonNull final String ansiColor) {
    this.value = value;
    this.level = level;
    this.ansiColor = ansiColor;
  }

  /**
   * Check if a log level is enabled
   *
   * @param level The log level to check
   * @return True if the level is enabled, false otherwise
   */
  public static boolean isLevelEnabled(@NonNull final AgentLogLevels level) {
    return level.value >= globalLevel.value;
  }

  public static boolean isTraceEnabled() {
    return isLevelEnabled(AgentLogLevels.TRACE);
  }

  public static boolean isDebugEnabled() {
    return isLevelEnabled(AgentLogLevels.DEBUG);
  }

  public static boolean isInfoEnabled() {
    return isLevelEnabled(AgentLogLevels.INFO);
  }

  public static boolean isWarnEnabled() {
    return isLevelEnabled(AgentLogLevels.WARN);
  }

  public static boolean isErrorEnabled() {
    return isLevelEnabled(AgentLogLevels.ERROR);
  }

  public static boolean isFatalEnabled() {
    return isLevelEnabled(AgentLogLevels.FATAL);
  }
}
