package com.mrtripop.spike.springcloudaws.config;

import com.mrtripop.spike.springcloudaws.util.RedisIAMAuthCredentialsProvider;
import com.mrtripop.spike.springcloudaws.util.RedisIAMAuthCredentialsProviderFactory;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.protocol.ProtocolVersion;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.RedisCredentialsProviderFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  @Value("${spring.redis.user}")
  private String user;

  @Value("${spring.redis.host}")
  private String host;

  @Value("${spring.redis.port}")
  private Integer port;

  @Bean
  public LettuceConnectionFactory redisConnectionFactory() {
    // create initial token for first connect redis
//    RedisIAMAuthCredentialsProviderFactory credentialsProviderFactory =
//        new RedisIAMAuthCredentialsProviderFactory();
//    RedisIAMAuthCredentialsProvider iamAuthCredentialsProvider =
//        (RedisIAMAuthCredentialsProvider) credentialsProviderFactory.getCredentialsProvider();

    // redis initial config
    RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration();
    redisClusterConfiguration.clusterNode(host, port);
    redisClusterConfiguration.setUsername(user);
//    redisClusterConfiguration.setPassword(iamAuthCredentialsProvider.getIamAuthToken());

    System.out.println(
        "password is present: " + redisClusterConfiguration.getPassword().isPresent());

    // create refresh token for next time password rotate
    RedisCredentialsProviderFactory redisCredentialsProviderFactory =
        new RedisIAMAuthCredentialsProviderFactory();
    redisCredentialsProviderFactory.createCredentialsProvider(redisClusterConfiguration);

    // redis client config
    LettuceClientConfiguration lettuceClientConfiguration =
        LettuceClientConfiguration.builder()
            .commandTimeout(Duration.ofSeconds(10))
            .shutdownTimeout(Duration.ofSeconds(10))
            .clientOptions(ClientOptions.builder().protocolVersion(ProtocolVersion.RESP2).build())
            // refresh password with credentials provider
            .redisCredentialsProviderFactory(redisCredentialsProviderFactory)
            .useSsl()
            .build();

    // connection factory
    LettuceConnectionFactory connectionFactory =
        new LettuceConnectionFactory(redisClusterConfiguration, lettuceClientConfiguration);
    connectionFactory.afterPropertiesSet();
    return connectionFactory;
  }

  @Bean
  public RedisTemplate<String, Object> template() {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(redisConnectionFactory());
    template.setKeySerializer(new StringRedisSerializer());
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    template.setHashValueSerializer((new GenericJackson2JsonRedisSerializer()));
    template.afterPropertiesSet();
    return template;
  }
}
