package com.cynthiasystems.agentnewsx.logging;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AgentLogFormat {

  /**
   * Simple string formatting helper that replaces {} placeholders with arguments
   *
   * @param template The message template with {} placeholders
   * @param args The arguments to substitute
   * @return The formatted string
   */
  public static String format(@NonNull final String template, @NonNull final Object... args) {
    if (args.length == 0) {
      return template;
    }

    final StringBuilder result = new StringBuilder();
    int templateIndex = 0;
    int argIndex = 0;

    while (templateIndex < template.length()) {
      final int placeholderIndex = template.indexOf("{}", templateIndex);

      if (placeholderIndex < 0 || argIndex >= args.length) {
        // No more placeholders or args, append the rest of the template
        result.append(template.substring(templateIndex));
        break;
      }

      // Append the text up to the placeholder
      result.append(template, templateIndex, placeholderIndex);

      // Append the arg
      final Object arg = args[argIndex++];
      if (arg instanceof Throwable && argIndex == args.length) {
        // Last arg is a Throwable, don't include it here
        result.append("{}");
      } else {
        result.append(arg);
      }

      // Move past this placeholder
      templateIndex = placeholderIndex + 2;
    }

    return result.toString();
  }
}
