package com.cynthiasystems.agentnewsx.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@UtilityClass
public class JsonUtils {
  static ObjectMapper objectMapper = new ObjectMapper();

  static {
    objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    objectMapper.findAndRegisterModules();
    objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  @SneakyThrows
  public static <T> T clone(@NonNull final T object, @NonNull final Class<T> tClass) {
    return toObject(toJson(object), tClass);
  }

  @SneakyThrows
  public static String toJson(@NonNull final Object object) {
    return objectMapper.writeValueAsString(object);
  }

  @SneakyThrows
  public static <T> T toObject(@NonNull final String json, @NonNull final Class<T> tClass) {
    return objectMapper.readValue(json, tClass);
  }

  @SneakyThrows
  public static String toJsonPretty(@NonNull final Object object) {
    return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
  }

  @SneakyThrows
  public static <T> T toObject(
      @NonNull final String json, @NonNull final TypeReference<T> typeReference) {
    return objectMapper.readValue(json, typeReference);
  }

  @SneakyThrows
  public static <T> T typeCast(@NonNull final Object object, @NonNull final Class<T> tClass) {
    return toObject(toJson(object), tClass);
  }

  @SneakyThrows
  public static <T> T typeCast(
      @NonNull final Object object, @NonNull final TypeReference<T> typeReference) {
    return toObject(toJson(object), typeReference);
  }
}
