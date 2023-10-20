package com.mrtripop.spike.springcloudaws.util;

import io.lettuce.core.RedisCredentials;
import io.lettuce.core.RedisCredentialsProvider;
import reactor.core.publisher.Mono;

public class RedisStaticCredentialsProvider implements RedisCredentialsProvider {
  private final String userName;
  private final String password;

  public RedisStaticCredentialsProvider(String userName, String password) {
    this.userName = userName;
    this.password = password;
  }

  @Override
  public Mono<RedisCredentials> resolveCredentials() {
    return Mono.just(RedisCredentials.just(userName, password));
  }
}
