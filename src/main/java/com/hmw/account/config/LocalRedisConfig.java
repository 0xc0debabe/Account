package com.hmw.account.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisExecProvider;
import redis.embedded.RedisServer;
import redis.embedded.util.OS;

@Configuration
public class LocalRedisConfig {
    @Value("${spring.redis.port}")
    private int redisPort;

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() {
        try {
            redisServer = RedisServer.builder()
                    .port(redisPort)
                    .setting("bind 127.0.0.1")
                    .redisExecProvider(RedisExecProvider.defaultProvider()
                            .override(OS.MAC_OS_X, "/opt/homebrew/opt/redis/bin/redis-server"))
                    .build();
            redisServer.start();
            System.out.println("Redis started on port " + redisPort);
        } catch (Exception e) {
            System.err.println("Error starting Redis server: " + e.getMessage());
        }
    }


    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }
}
