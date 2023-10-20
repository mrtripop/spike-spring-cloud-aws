package com.mrtripop.spike.springcloudaws.util;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import io.lettuce.core.RedisCredentials;
import io.lettuce.core.RedisCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.lettuce.RedisCredentialsProviderFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class RedisIAMAuthCredentialsProviderFactory implements RedisCredentialsProviderFactory {

  @Value("${spring.redis.user}")
  private String user;

  @Value("${spring.redis.replication.group}")
  private String replicationGroupId;

  @Value("${spring.redis.replication.group.region}")
  private String region;

  @Nullable
  @Override
  public RedisCredentialsProvider createCredentialsProvider(RedisConfiguration redisConfiguration) {
    return () -> getCredentialsProvider().resolveCredentials();
  }

  public RedisCredentialsProvider getCredentialsProvider() {
    AWSCredentialsProvider awsCredentialsProvider = new DefaultAWSCredentialsProviderChain();
    IAMAuthTokenRequest iamAuthTokenRequest =
        new IAMAuthTokenRequest(user, replicationGroupId, region);
    return new RedisIAMAuthCredentialsProvider(user, iamAuthTokenRequest, awsCredentialsProvider);
  }
}
