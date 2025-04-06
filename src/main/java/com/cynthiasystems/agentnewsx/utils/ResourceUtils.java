package com.cynthiasystems.agentnewsx.utils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

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
}
