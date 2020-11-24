package com.service.zgbj.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.List;

/**
 */
public class JedisPoolManager {

    private static final Logger log = LoggerFactory.getLogger(JedisPoolManager.class);

    private static final ThreadLocal<JedisPool> CURRENT_JEDIS_POOL = new ThreadLocal<>();

    private List<JedisPool> jedisPools;
    private String password;

    public Jedis getJedis() {
        for (int i = 0, size = jedisPools.size(); i < size; i++) {
            JedisPool jedisPool = jedisPools.get(i);
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                if (jedis.isConnected()) {
                    CURRENT_JEDIS_POOL.set(jedisPool);
                    return jedis;
                } else {
                    jedis.close();
                    log.error("Get jedis connection from pool but not connected.");
                }
            } catch (JedisConnectionException e) {
                log.error("Get jedis connection from pool list index:{}", i, e);
                if (jedis != null) {
                    log.warn("Return broken resource:" + jedis);
                    jedis.close();
                }
            }
        }
        return null;
    }

    public void returnJedis(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
            CURRENT_JEDIS_POOL.remove();
        }
    }

    public void setJedisPools(List<JedisPool> jedisPools) {
        this.jedisPools = jedisPools;
    }

    public List<JedisPool> getJedisPools() {
        return jedisPools;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
