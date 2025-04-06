package com.cynthiasystems.agentnewsx.utils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ResourceUtils {

  @SneakyThrows
  public static String readStringResource(@NonNull final String resourcePath) {
    @Cleanup InputStream inputStream = ResourceUtils.class.getResourceAsStream(resourcePath);
    return new String(
        IOUtils.toByteArray(Optional.ofNullable(inputStream).orElseThrow()),
        StandardCharsets.UTF_8);
  }

  @SneakyThrows
  public static InputStream readStringResourceToInputStream(@NonNull final String resourcePath) {
    return ResourceUtils.class.getResourceAsStream(resourcePath);
  }

  @SneakyThrows
  public static Properties readApplicationProperties() {
    @Cleanup
    final InputStream inputStream = readStringResourceToInputStream("/application.properties");
    final Properties properties = new Properties();
    properties.load(inputStream);
    return properties;
  }
}
