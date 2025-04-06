package com.cynthiasystems.agentnewsx.utils;

import java.net.MalformedURLException;
import java.net.URL;

import com.cynthiasystems.agentnewsx.logging.AgentLog;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class URLUtils {
  /**
   * Resolves a potentially relative URL against a base URL.
   *
   * @param baseUrl The base URL to resolve against
   * @param href The potentially relative URL
   * @return The resolved absolute URL or null if invalid
   */
  public static String resolveUrl(@NonNull final String baseUrl, @NonNull final String href) {
    try {
      // If href is already absolute, return it
      if (href.matches("^https?://.*")) {
        return href;
      }

      final URL base = new URL(baseUrl);
      final URL resolved = new URL(base, href);
      return resolved.toString();
    } catch (final MalformedURLException e) {
      AgentLog.warn("Failed to resolve URL: " + href + " against base: " + baseUrl);
      return null;
    }
  }
}
