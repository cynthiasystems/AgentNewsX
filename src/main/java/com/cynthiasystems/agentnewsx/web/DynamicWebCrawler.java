package com.cynthiasystems.agentnewsx.web;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.cynthiasystems.agentnewsx.logging.AgentLog;

import lombok.experimental.UtilityClass;

/** Utility for crawling dynamic web pages with JavaScript support. */
@UtilityClass
public class DynamicWebCrawler {
  /**
   * Loads a dynamic web page and returns its fully rendered HTML content.
   *
   * @param url The URL to load
   * @return The fully rendered HTML with JavaScript executed
   */
  public static String getRenderedHtml(String url) {
    ChromeDriver driver = null;

    try {
      // Configure Chrome in headless mode
      ChromeOptions options = new ChromeOptions();
      options.addArguments("--headless");
      options.addArguments("--disable-gpu");
      options.addArguments("--no-sandbox");
      options.addArguments("--disable-dev-shm-usage");

      // Add headers
      options.addArguments(
          "--user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

      // Set accept headers via preferences
      Map<String, Object> prefs = new HashMap<>();
      prefs.put("profile.default_content_settings.cookies", 2);
      prefs.put("intl.accept_languages", "en-US,en;q=0.9");
      options.setExperimentalOption("prefs", prefs);

      // Set additional header options via CDP
      options.setExperimentalOption("excludeSwitches", new String[] {"enable-automation"});
      options.setExperimentalOption("useAutomationExtension", false);

      // Initialize driver
      driver = new ChromeDriver(options);

      // Add additional headers via CDP
      Map<String, Object> headers = new HashMap<>();
      headers.put(
          "Accept",
          "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");

      // Execute CDP command to set headers
      driver.executeCdpCommand("Network.setExtraHTTPHeaders", Map.of("headers", headers));

      AgentLog.info("Loading dynamic page content: " + url);
      driver.get(url);

      // Wait for page to load
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      wait.until(
          webDriver ->
              ("complete"
                  .equals(
                      ((JavascriptExecutor) webDriver)
                          .executeScript("return document.readyState"))));

      // Scroll to trigger lazy loading
      JavascriptExecutor js = driver;
      js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
      Thread.sleep(1000);

      // Get the fully rendered HTML
      String pageSource = driver.getPageSource();
      AgentLog.info("Successfully retrieved dynamic content for: " + url);
      return pageSource;

    } catch (Exception e) {
      AgentLog.error("Error getting dynamic page content: " + url + " - " + e.getMessage());
      return null;
    } finally {
      // Always close the driver
      if (driver != null) {
        try {
          driver.quit();
        } catch (Exception e) {
          AgentLog.warn("Error closing WebDriver: " + e.getMessage());
        }
      }
    }
  }
}
