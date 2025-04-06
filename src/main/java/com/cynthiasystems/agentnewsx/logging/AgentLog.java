package com.cynthiasystems.agentnewsx.logging;

import static com.cynthiasystems.agentnewsx.logging.AgentLogFormat.format;
import static com.cynthiasystems.agentnewsx.logging.AgentLogLevels.isDebugEnabled;
import static com.cynthiasystems.agentnewsx.logging.AgentLogLevels.isErrorEnabled;
import static com.cynthiasystems.agentnewsx.logging.AgentLogLevels.isFatalEnabled;
import static com.cynthiasystems.agentnewsx.logging.AgentLogLevels.isInfoEnabled;
import static com.cynthiasystems.agentnewsx.logging.AgentLogLevels.isTraceEnabled;
import static com.cynthiasystems.agentnewsx.logging.AgentLogLevels.isWarnEnabled;
import static com.cynthiasystems.agentnewsx.logging.AgentLogWriter.log;

import java.util.function.Supplier;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/** KGLog - A simple, static utility logging class with zero dependencies */
@UtilityClass
public class AgentLog {

  public static void trace(@NonNull final String message) {
    log(AgentLogLevels.TRACE, message);
  }

  public static void trace(@NonNull final Supplier<String> messageSupplier) {
    if (isTraceEnabled()) {
      log(AgentLogLevels.TRACE, messageSupplier.get());
    }
  }

  public static void trace(@NonNull final String message, @NonNull final Object... args) {
    if (isTraceEnabled()) {
      log(AgentLogLevels.TRACE, format(message, args));
    }
  }

  public static void trace(@NonNull final String message, @NonNull final Throwable throwable) {
    log(AgentLogLevels.TRACE, message, throwable);
  }

  public static void debug(@NonNull final String message) {
    log(AgentLogLevels.DEBUG, message);
  }

  public static void debug(@NonNull final Supplier<String> messageSupplier) {
    if (isDebugEnabled()) {
      log(AgentLogLevels.DEBUG, messageSupplier.get());
    }
  }

  public static void debug(@NonNull final String message, @NonNull final Object... args) {
    if (isDebugEnabled()) {
      log(AgentLogLevels.DEBUG, format(message, args));
    }
  }

  public static void debug(@NonNull final String message, @NonNull final Throwable throwable) {
    log(AgentLogLevels.DEBUG, message, throwable);
  }

  public static void info(@NonNull final String message) {
    log(AgentLogLevels.INFO, message);
  }

  public static void info(@NonNull final Supplier<String> messageSupplier) {
    if (isInfoEnabled()) {
      log(AgentLogLevels.INFO, messageSupplier.get());
    }
  }

  public static void info(@NonNull final String message, @NonNull final Object... args) {
    if (isInfoEnabled()) {
      log(AgentLogLevels.INFO, format(message, args));
    }
  }

  public static void info(@NonNull final String message, @NonNull final Throwable throwable) {
    log(AgentLogLevels.INFO, message, throwable);
  }

  public static void warn(@NonNull final String message) {
    log(AgentLogLevels.WARN, message);
  }

  public static void warn(@NonNull final Supplier<String> messageSupplier) {
    if (isWarnEnabled()) {
      log(AgentLogLevels.WARN, messageSupplier.get());
    }
  }

  public static void warn(@NonNull final String message, @NonNull final Object... args) {
    if (isWarnEnabled()) {
      log(AgentLogLevels.WARN, format(message, args));
    }
  }

  public static void warn(@NonNull final String message, @NonNull final Throwable throwable) {
    log(AgentLogLevels.WARN, message, throwable);
  }

  public static void error(@NonNull final String message) {
    log(AgentLogLevels.ERROR, message);
  }

  public static void error(@NonNull final Supplier<String> messageSupplier) {
    if (isErrorEnabled()) {
      log(AgentLogLevels.ERROR, messageSupplier.get());
    }
  }

  public static void error(@NonNull final String message, @NonNull final Object... args) {
    if (isErrorEnabled()) {
      log(AgentLogLevels.ERROR, format(message, args));
    }
  }

  public static void error(@NonNull final String message, @NonNull final Throwable throwable) {
    log(AgentLogLevels.ERROR, message, throwable);
  }

  public static void fatal(@NonNull final String message) {
    log(AgentLogLevels.FATAL, message);
  }

  public static void fatal(@NonNull final Supplier<String> messageSupplier) {
    if (isFatalEnabled()) {
      log(AgentLogLevels.FATAL, messageSupplier.get());
    }
  }

  public static void fatal(@NonNull final String message, @NonNull final Object... args) {
    if (isFatalEnabled()) {
      log(AgentLogLevels.FATAL, format(message, args));
    }
  }

  public static void fatal(@NonNull final String message, @NonNull final Throwable throwable) {
    log(AgentLogLevels.FATAL, message, throwable);
  }
}
