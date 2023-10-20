package com.mrtripop.spike.springcloudaws.util;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.google.common.base.Suppliers;
import io.lettuce.core.RedisCredentials;
import io.lettuce.core.RedisCredentialsProvider;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class RedisIAMAuthCredentialsProvider implements RedisCredentialsProvider {
  private final String userName;
  private final IAMAuthTokenRequest iamAuthTokenRequest;
  private final AWSCredentialsProvider awsCredentialsProvider;
  private final Supplier<String> iamAuthTokenProvider;
  private static final long TOKEN_CACHE_SECONDS = 600;

  public RedisIAMAuthCredentialsProvider(
      String userName,
      IAMAuthTokenRequest iamAuthTokenRequest,
      AWSCredentialsProvider awsCredentialsProvider) {
    this.userName = userName;
    this.iamAuthTokenRequest = iamAuthTokenRequest;
    this.awsCredentialsProvider = awsCredentialsProvider;
    this.iamAuthTokenProvider =
        Suppliers.memoizeWithExpiration(
            this::getIamAuthToken, TOKEN_CACHE_SECONDS, TimeUnit.SECONDS);
  }

  @Override
  public Mono<RedisCredentials> resolveCredentials() {
    RedisCredentials redisCredentials = RedisCredentials.just(userName, iamAuthTokenProvider.get());
    System.out.println("[resolveCredentials] Generate credentials again");
    return Mono.just(redisCredentials);
  }

  public String getIamAuthToken() {
    try {
      return iamAuthTokenRequest.toSignedRequestUri(awsCredentialsProvider.getCredentials());
    } catch (URISyntaxException e) {
      throw new RuntimeException("Error when creating IAM Auth token", e);
    }
  }
}
