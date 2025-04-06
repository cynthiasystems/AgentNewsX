package com.cynthiasystems.agentnewsx.logging;

import static com.cynthiasystems.agentnewsx.logging.AgentLogLevels.isLevelEnabled;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import lombok.NonNull;

public class AgentLogWriter {
  private static final String ANSI_RESET = "\u001B[0m";
  private static final boolean useColors = true;
  private static final boolean includeThreadName = true;

  /**
   * Log a message at the specified level
   *
   * @param level The log level
   * @param message The message to log
   */
  public static void log(@NonNull final AgentLogLevels level, @NonNull final String message) {
    if (isLevelEnabled(level)) {
      logInternal(level, message);
    }
  }

  /**
   * Log a message and throwable at the specified level
   *
   * @param level The log level
   * @param message The message to log
   * @param throwable The throwable to log
   */
  public static void log(
      @NonNull final AgentLogLevels level,
      @NonNull final String message,
      @NonNull final Throwable throwable) {
    if (isLevelEnabled(level)) {
      logInternal(level, message, throwable);
    }
  }

  /**
   * Internal logging implementation
   *
   * @param level The log level
   * @param message The message
   */
  private static void logInternal(
      @NonNull final AgentLogLevels level, @NonNull final String message) {
    final SimpleDateFormat dateFormat =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
    final String timestamp = dateFormat.format(new Date());
    final String threadInfo =
        includeThreadName ? "[" + Thread.currentThread().getName() + "] " : "";
    final String levelStr = level.level();

    // Get calling class name (skipping stack frames for this utility class)
    final String callerClassName = getCallerClassName();

    // Format the log message
    final String formattedMessage =
        String.format(
            "%s %s[%s] %s - %s", timestamp, threadInfo, levelStr, callerClassName, message);

    // Apply colors if enabled (console only)
    final String coloredMessage =
        useColors ? level.ansiColor() + formattedMessage + ANSI_RESET : formattedMessage;

    // Log to console
    System.out.println(coloredMessage);
  }

  /**
   * Internal logging implementation
   *
   * @param level The log level
   * @param message The message
   * @param throwable The throwable (optional)
   */
  private static void logInternal(
      @NonNull final AgentLogLevels level,
      @NonNull final String message,
      @NonNull final Throwable throwable) {
    logInternal(level, message);
    System.err.println(throwable.getMessage());
  }

  /**
   * Get the name of the class that called the logging method
   *
   * @return The caller class name
   */
  private static String getCallerClassName() {
    final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

    // Find the first class in the stack that isn't this utility class
    for (int i = 1; i < stackTrace.length; i++) {
      final String className = stackTrace[i].getClassName();
      final String thisClassName = AgentLog.class.getName();
      if (!className.equals(thisClassName) && !className.equals(Thread.class.getName())) {
        final int lastDot = className.lastIndexOf('.');
        return lastDot > 0 ? className.substring(lastDot + 1) : className;
      }
    }

    return "Unknown";
  }
}
