package com.service.zgbj.config;

import com.service.zgbj.redis.JedisPoolManager;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Arrays;

/**
 * @Author yubin
 * @Description //TODO
 * @Date 2018/12/28 下午9:38
 */

@Data
@Configuration
@PropertySource(value = "classpath:redis.properties",ignoreResourceNotFound = true)
public class JedisConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Value("${spring.redis.timeout}")
    private int timeout;

    @Value("${spring.redis.jedis.pool.max-active}")
    private int maxTotal;

    @Value("${spring.redis.jedis.pool.max-idle}")
    private int maxIdle;

    @Value("${spring.redis.jedis.pool.min-idle}")
    private int minIdle;

    @Value("${spring.redis.jedis.pool.max-wait}")
    private long maxWaitMillis;

    @Value("${spring.redis.password}")
    private String password;

    private int reconnectionTimeout = 3000;
    private int database = 0;
    private String codec = "org.redisson.codec.JsonJacksonCodec";


    @Bean
    public JedisPool redisPoolFactory() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(maxTotal);
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
        jedisPoolConfig.setMinIdle(minIdle);
        jedisPoolConfig.setTestOnBorrow(true);
        jedisPoolConfig.setTestOnReturn(true);
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, host, port, timeout, password);
        return jedisPool;
    }

    @Bean
    public JedisPoolManager getJedisPoolManager(){
        JedisPoolManager jedisPoolManager = new JedisPoolManager();
        jedisPoolManager.setJedisPools(Arrays.asList(redisPoolFactory()));

        return jedisPoolManager;
    }

}
