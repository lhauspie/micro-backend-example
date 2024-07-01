package com.lhauspie.example;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class ClientLibrary {
  private final String providerUrl;
  private final Duration cacheDuration;
  private LocalDateTime cacheLocalDateTime = LocalDateTime.MIN;
  private String businessLogicScript = null;
  private String lastModified = null;

  private final OkHttpClient client = new OkHttpClient();

  private final ObjectMapper objectMapper = new ObjectMapper()
      .registerModule(new Jdk8Module())
      .setSerializationInclusion(JsonInclude.Include.ALWAYS)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);

  public ClientLibrary(String providerUrl, Duration cacheDuration) {
    this.providerUrl = providerUrl;
    this.cacheDuration = cacheDuration;
  }

  public BusinessLogicResponse executeBusinessLogic(Object input) {
    try {
      return toObject(executeBusinessLogic(toMap(input)), BusinessLogicResponse.class);
    } catch (Exception e) {
      e.printStackTrace();
      return BusinessLogicResponse.error(
          List.of(
              new BusinessLogicResponse.Error(
                  "CLIENT_LIB",
                  Optional.empty(),
                  e.getMessage()
              )
          )
      );
    }
  }

  private boolean isCacheValid() {
    LocalDateTime now = LocalDateTime.now();
    return Duration.between(this.cacheLocalDateTime, now).minus(this.cacheDuration).isNegative();
  }

  private String fetchBusinessLogic() throws IOException {
    if (this.isCacheValid()) {
      return this.businessLogicScript;
    }

    Request.Builder requestBuilder = new Request.Builder().url(this.providerUrl + "/business-logic.js");
    if (this.lastModified != null) {
      requestBuilder.header("If-Modified-Since", this.lastModified);
    }
    Request request = requestBuilder.build();

    try (Response response = client.newCall(request).execute()) {
      if (response.code() == 200) {
        this.cache(response);
        return this.businessLogicScript;
      } else if (response.code() == 304) {
        this.cache();
        return this.businessLogicScript;
      } else {
        if (this.businessLogicScript != null) {
          return this.businessLogicScript;
        }
        throw new IOException("Failed to fetch business logic and no cache available: " + response.message());
      }
    }
  }

  private void cache(Response response) throws IOException {
    if (response != null) {
      this.businessLogicScript = response.body().string();
      this.lastModified = response.header("Last-Modified");
    }
    this.cacheLocalDateTime = LocalDateTime.now();
  }

  private void cache() {
    this.cacheLocalDateTime = LocalDateTime.now();
  }

  private Map<String, Object> executeBusinessLogic(Map<String, Object> input) throws Exception {
      String script = this.fetchBusinessLogic();
      if (script != null) {
        try (Context context = Context.create()) {
          // Define module and exports
          context.eval("js", "var exports = {}; var module = { exports: exports }; " + script);

          // Get the module.exports object
          Value moduleExports = context.eval("js", "module.exports");
          // Get the execute function from module.exports
          Value executeFunction = moduleExports.getMember("execute");

          // Execute the function with the input
          Value result = executeFunction.execute(input);

          // Convert the result to a Map
          return convertToMap(result);
        }
      } else {
        throw new Exception("Failed to load business logic script");
      }
  }


  private Map<String, Object> convertToMap(Value value) {
    Map<String, Object> resultMap = new HashMap<>();
    for (String key : value.getMemberKeys()) {
      Value member = value.getMember(key);
      if (member.hasArrayElements()) {
        resultMap.put(key, convertToList(member));
      } else if (member.hasMembers()) {
        resultMap.put(key, convertToMap(member));
      } else {
        resultMap.put(key, member.as(Object.class));
      }
    }
    return resultMap;
  }

  private List<Object> convertToList(Value value) {
    List<Object> list = new ArrayList<>();
    for (long i = 0; i < value.getArraySize(); i++) {
      Value element = value.getArrayElement(i);
      if (element.hasMembers()) {
        list.add(convertToMap(element));
      } else {
        list.add(element.as(Object.class));
      }
    }
    return list;
  }


  private Map<String, Object> toMap(Object input) throws JsonProcessingException {
    String jsonString = objectMapper.writeValueAsString(input);
    return objectMapper.readValue(jsonString, Map.class);
  }

  private <T> T toObject(Map<String, Object> input, Class<T> valueType) throws JsonProcessingException {
    String jsonString = objectMapper.writeValueAsString(input);
    return objectMapper.readValue(jsonString, valueType);
  }
}
