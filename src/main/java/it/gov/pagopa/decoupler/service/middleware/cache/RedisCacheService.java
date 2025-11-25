package it.gov.pagopa.decoupler.service.middleware.cache;

import io.quarkus.redis.datasource.RedisDataSource;
import io.vertx.mutiny.redis.client.RedisAPI;
import io.vertx.mutiny.redis.client.Response;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.util.List;

@ApplicationScoped
public class RedisCacheService {

  private final RedisDataSource syncClient;

  private final RedisAPI asyncClient;

  public RedisCacheService(RedisDataSource syncClient, RedisAPI asyncClient) {

    this.syncClient = syncClient;
    this.asyncClient = asyncClient;
  }

  public void store(String key, String value, int ttl) {

    this.asyncClient.set(List.of(key, value, "EX", String.valueOf(ttl)));
  }

  public String lookup(String key, String defaultValue) {

    Response cachedValue = asyncClient.get(key).await().atMost(Duration.ofMillis(500));
    return cachedValue == null ? defaultValue : cachedValue.toString();
  }
}
