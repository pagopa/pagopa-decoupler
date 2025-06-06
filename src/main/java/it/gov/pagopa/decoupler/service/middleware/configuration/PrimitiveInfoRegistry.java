package it.gov.pagopa.decoupler.service.middleware.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.decoupler.service.model.PrimitiveInfo;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class PrimitiveInfoRegistry {

  public static final String PRIMITIVE_INFO_FILE = "config/primitives.json";

  private final Map<String, PrimitiveInfo> mappingByPrimitive;

  private final Map<String, PrimitiveInfo> mappingByPath;

  public PrimitiveInfoRegistry() {

    this.mappingByPrimitive = new HashMap<>();
    this.mappingByPath = new HashMap<>();
  }

  @PostConstruct
  void init() {

    try (InputStream inputStream =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(PRIMITIVE_INFO_FILE)) {

      if (inputStream == null) {
        throw new RuntimeException(
            "Cannot load primitive info. Missing primitives configuration file.");
      }

      Map<String, PrimitiveInfo> loaded =
          new ObjectMapper().readValue(inputStream, new TypeReference<>() {});
      this.mappingByPrimitive.putAll(loaded);

      for (Map.Entry<String, PrimitiveInfo> entry : mappingByPrimitive.entrySet()) {

        PrimitiveInfo value = entry.getValue();
        value.setPrimitive(entry.getKey());
        this.mappingByPath.put(value.getExternalSubpath(), value);
      }

    } catch (IOException e) {
      throw new RuntimeException("Failed to load primitives info from configuration file", e);
    }
  }

  public PrimitiveInfo fromPrimitive(String primitive) {
    return this.mappingByPrimitive.get(primitive.toLowerCase());
  }

  public PrimitiveInfo fromPath(String externalSubpath) {
    return this.mappingByPath.get(externalSubpath.toLowerCase());
  }
}
