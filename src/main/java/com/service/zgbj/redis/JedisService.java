package com.service.zgbj.redis;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Tuple;

import java.util.*;

/**
 */
@Service
public class JedisService {

    private static final Logger LOG = LoggerFactory.getLogger(JedisService.class);

    @Autowired
    private JedisPoolManager readJedisPoolManager;

    @Autowired
    private JedisPoolManager writeJedisPoolManager;



    public void setex(String key, int seconds, String value) {
        Jedis wjedis = null;
        try {
            wjedis = writeJedisPoolManager.getJedis();
            wjedis.setex(key, seconds, value);
        } catch (Exception e) {
            LOG.error("setex from jedis error. key:{} value:{}", key, value);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
    }

    public Boolean setnx(String key, String value, Integer expireSeconds) {
        Jedis wjedis = null;
        try {
            wjedis = this.writeJedisPoolManager.getJedis();
            long result = wjedis.setnx(key, value);
            if (result == 1) {
                return Boolean.TRUE;
            }
        } catch (Exception e) {
            LOG.error("zincrby from jedis error. key:{} score:{} member:{}", key);
        } finally {
            try {
                wjedis.expire(key, expireSeconds);
            } catch (Exception e) {
                LOG.error("设置超时时间失败[key={}, value={},expireSeconds={}]", key, value, expireSeconds, e);
            }

            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }

        return Boolean.FALSE;
    }

    public byte[] readByte(String key) throws Exception {
        byte[] ret = null;
        Jedis rjedis = null;
        try {
            rjedis = readJedisPoolManager.getJedis();
            byte[] jedisKey = key.getBytes();
            byte[] content = rjedis.get(jedisKey);
            if (content != null && content.length > 0) {
                ret = content;
            }
        } catch (Exception e) {
            LOG.error("read from jedis error. key:{} msg:{}", key, e);
            throw new Exception("redis read error.", e);
        } finally {
            if (rjedis != null) {
                readJedisPoolManager.returnJedis(rjedis);
            }
        }
        return ret;
    }

    /**
     * 写入redis，并指定失效时间点
     *
     * @param key
     * @param content  数据
     * @param deadLine 失效时间点
     */
    public void write(String key, String content, Date deadLine) {
        Jedis wjedis = null;
        long now = System.currentTimeMillis();
        long dead = deadLine.getTime();
        int expireTime = (int) (dead - now) / (1000 * 60);//转换为分钟
        if (expireTime <= 0) {
            LOG.warn("request ignored .Date:{} msg:{}", new Object[]{deadLine, " invalid deadLine:The deadLine must be one minute later than currentTime "});
            return;
        } else {
            try {
                wjedis = writeJedisPoolManager.getJedis();
                byte[] data = null;
                if (content != null) {
                    data = content.getBytes();
                }
                byte[] jedisKey = key.getBytes();
                wjedis.setex(jedisKey, expireTime, data);
            } catch (Exception e) {
//                LOG.error("write to jedis error. key:{} data:{} msg:{}", key, content, e);
            } finally {
                if (wjedis != null) {
                    writeJedisPoolManager.returnJedis(wjedis);
                }
            }
        }
    }

    public void write(String key, byte[] content, int expireTime) throws Exception {
        Jedis wjedis = null;
        try {
            wjedis = writeJedisPoolManager.getJedis();
            byte[] data = content;
            byte[] jedisKey = key.getBytes();
            wjedis.setex(jedisKey, expireTime, data);
        } catch (Exception e) {
            throw new Exception("Failed to write key " + key, e);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
    }

    /**
     * 写入redis，并指定失效时间点
     *
     * @param key
     * @param content    数据
     * @param expireTime 失效时长(秒)
     */
    public void write(String key, String content, int expireTime) {
        Jedis wjedis = null;
        try {
            wjedis = writeJedisPoolManager.getJedis();
            byte[] data = null;
            if (content != null) {
                data = content.getBytes();
            }
            byte[] jedisKey = key.getBytes();

            wjedis.setex(jedisKey, expireTime, data);
        } catch (Exception e) {
//            LOG.error("write to jedis error. key:{} data:{} msg:{}", key, content, e);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
    }

    public void set(String key, String content) {
        Jedis wjedis = null;
        try {
            wjedis = writeJedisPoolManager.getJedis();
            wjedis.set(key, content);
        } catch (Exception e) {
            LOG.error("set to jedis error. key:{} data:{} msg:{}", key, content, e);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
    }

    public void set(String key, String content, int expire) {
        Jedis wjedis = null;
        try {
            wjedis = writeJedisPoolManager.getJedis();
            wjedis.set(key, content);
            if (expire >= 0) {
                wjedis.expire(key, expire);
            }
        } catch (Exception e) {
            LOG.error("set to jedis error. key:{} data:{} msg:{}", key, content, e);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
    }

    /**
     * SET key value [EX seconds] [PX milliseconds] [NX|XX]
     *
     *
     * @param key
     * @param value
     * @param nxxx
     *      NX ：只在键不存在时，才对键进行设置操作。 SET key value NX 效果等同于 SETNX key value 。
     *      XX ：只在键已经存在时，才对键进行设置操作。
     * @param expx
     *      EX second ：设置键的过期时间为 second 秒。 SET key value EX second 效果等同于 SETEX key second value 。
     *      PX millisecond ：设置键的过期时间为 millisecond 毫秒。 SET key value PX millisecond 效果等同于 PSETEX key millisecond value 。
     * @param time
     */
    public String set(String key, String value, String nxxx, String expx, long time) {
        Jedis wjedis = null;
        try {
            wjedis = writeJedisPoolManager.getJedis();
            return wjedis.set(key, value, nxxx, expx, time);
        } catch (Exception e) {
            LOG.error("set to jedis error. key:{} data:{} msg:{}, expire:{}", key, value, time, e);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
        return "";
    }

    public Long del(String key) {
        Jedis wjedis = null;
        try {
            wjedis = writeJedisPoolManager.getJedis();
            return wjedis.del(key);
        } catch (Exception e) {
            LOG.error("delete from jedis error. key:{}", key);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
        return 0L;
    }

    public String get(String key) {
        String ret = null;
        Jedis rjedis = null;
        try {
            rjedis = readJedisPoolManager.getJedis();
            ret = rjedis.get(key);
        } catch (Exception e) {
            LOG.error("get from jedis error. key:{}", key);
        } finally {
            if (rjedis != null) {
                readJedisPoolManager.returnJedis(rjedis);
            }
        }
        return ret;
    }

    public String read(String key) {
        String ret = null;
        Jedis rjedis = null;
        try {
            rjedis = readJedisPoolManager.getJedis();
            ret = rjedis.get(key);
        } catch (Exception e) {
            LOG.error("hget from jedis error. key:{} field:{}", key);
        } finally {
            if (rjedis != null) {
                readJedisPoolManager.returnJedis(rjedis);
            }
        }
        return ret;
    }

    public Long incr(String key) {
        Long wet = null;
        Jedis wjedis = null;
        try {
            wjedis = writeJedisPoolManager.getJedis();
            wet = wjedis.incr(key);
        } catch (Exception e) {
            LOG.error("incr from jedis error. key:{}", key);
        } finally {
            if (wjedis != null) {
                readJedisPoolManager.returnJedis(wjedis);
            }
        }
        return wet;
    }

    public Long incrBy(String key, long inet) {
        Long ret = null;
        Jedis rjedis = null;
        try {
            rjedis = readJedisPoolManager.getJedis();
            ret = rjedis.incrBy(key, inet);
        } catch (Exception e) {
            LOG.error("incrBy from jedis error. key:{} inet:{}", key, inet);
        } finally {
            if (rjedis != null) {
                readJedisPoolManager.returnJedis(rjedis);
            }
        }
        return ret;
    }

    public Long decr(String key) {
        Long wet = null;
        Jedis wjedis = null;
        try {
            wjedis = writeJedisPoolManager.getJedis();
            wet = wjedis.decr(key);
        } catch (Exception e) {
            LOG.error("decr from jedis error. key:{}", key);
        } finally {
            if (wjedis != null) {
                readJedisPoolManager.returnJedis(wjedis);
            }
        }
        return wet;
    }

    public Long decrBy(String key, long inet) {
        Long ret = null;
        Jedis rjedis = null;
        try {
            rjedis = readJedisPoolManager.getJedis();
            ret = rjedis.decrBy(key, inet);
        } catch (Exception e) {
            LOG.error("decrBy from jedis error. key:{} inet:{}", key, inet);
        } finally {
            if (rjedis != null) {
                readJedisPoolManager.returnJedis(rjedis);
            }
        }
        return ret;
    }

    public void lpush(String key, String... values) {
        Jedis wjedis = null;
        try {
            wjedis = writeJedisPoolManager.getJedis();
            wjedis.lpush(key, values);
        } catch (Exception e) {
            LOG.error("lpush from jedis error. key:{}  value:{}", key, values);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
    }

    public String lpop(String key) {
        Jedis wjedis = null;
        try {
            wjedis = writeJedisPoolManager.getJedis();
            return wjedis.lpop(key);
        } catch (Exception e) {
            LOG.error("rpop from jedis error. key:{}  value:{}", key);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }

        return null;
    }

    public void rpush(String key, String... values) {
        Jedis wjedis = null;
        try {
            wjedis = writeJedisPoolManager.getJedis();
            wjedis.rpush(key, values);
        } catch (Exception e) {
            LOG.error("rpush from jedis error. key:{}  value:{}", key, values);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
    }

    public Long llen(String key) {
        Jedis rjedis = null;
        try {
            rjedis = readJedisPoolManager.getJedis();
            return rjedis.llen(key);
        } catch (Exception e) {
            LOG.error("llen from jedis error. key:{}", key);
        } finally {
            if (rjedis != null) {
                readJedisPoolManager.returnJedis(rjedis);
            }
        }
        return null;
    }

    public List<String> lrange(String key, long begin, long end) {
        Jedis rjedis = null;
        List<String> result = null;
        try {
            rjedis = readJedisPoolManager.getJedis();
            result = rjedis.lrange(key, begin, end);
            return result;
        } catch (Exception e) {
            LOG.error("llen from jedis error. key:{}", key);
        } finally {
            if (rjedis != null) {
                readJedisPoolManager.returnJedis(rjedis);
            }
        }
        return null;
    }

    public Long lrem(String key, long count, String value) {
        Jedis rjedis = null;
        Long result = 0L;
        try {
            rjedis = readJedisPoolManager.getJedis();
            result = rjedis.lrem(key, count, value);
            return result;
        } catch (Exception e) {
            LOG.error("lrem from jedis error. key:{}", key);
        } finally {
            if (rjedis != null) {
                readJedisPoolManager.returnJedis(rjedis);
            }
        }
        return result;
    }

    public String rpop(String key) {
        Jedis wjedis = null;
        try {
            wjedis = writeJedisPoolManager.getJedis();
            return wjedis.rpop(key);
        } catch (Exception e) {
            LOG.error("rpop from jedis error. key:{}  value:{}", key);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }

        return null;
    }

    public String hget(String key, String field) {
        String ret = null;
        Jedis rjedis = null;
        try {
            rjedis = readJedisPoolManager.getJedis();
            ret = rjedis.hget(key, field);
        } catch (Exception e) {
            LOG.error("hget from jedis error. key=" + key + "field=" + field, e);
        } finally {
            if (rjedis != null) {
                readJedisPoolManager.returnJedis(rjedis);
            }
        }
        return ret;
    }

    public void hset(String key, String field, String value) {
        Jedis wjedis = null;
        try {
            wjedis = writeJedisPoolManager.getJedis();
            Long hset = wjedis.hset(key, field, value);
        } catch (Exception e) {
            LOG.error("hset from jedis error. key:{} field:{} value:{}", key, field, value, e);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
    }

    /**
     * 将哈希表key中的域field的值设置为value，当且仅当域field不存在。
     * 若域field已经存在，该操作无效。
     * 如果key不存在，一个新哈希表被创建并执行HSETNX命令。
     *
     * @param key
     * @param field
     * @param value
     * @return
     *    设置成功，返回1。
     *    如果给定域已经存在且没有操作被执行，返回0。
     */
    public Long hsetnx(String key, String field, String value) {
        Jedis wjedis = null;
        try {
            wjedis = writeJedisPoolManager.getJedis();
            return wjedis.hsetnx(key, field, value);
        } catch (Exception e) {
            LOG.error("hset from jedis error. key:{} field:{} value:{}", key, field, value, e);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
        return null;
    }

    public Boolean hexists(String key, String field) {
        Boolean exists = false;
        Jedis wjedis = null;
        try {
            wjedis = writeJedisPoolManager.getJedis();
            exists = wjedis.hexists(key,field);
        } catch (Exception e) {
            LOG.error("hexists from jedis error. key:{} field:{}", key, field, e);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
        return exists;
    }

    public List<String> hmread(String key, String... field) {
        List<String> ret = null;
        Jedis rjedis = null;
        try {
            rjedis = readJedisPoolManager.getJedis();
            if (field.length > 0) {
                ret = rjedis.hmget(key, field);
            }
        } catch (Exception e) {
            LOG.error("hmread from jedis error. key={}, field={}", key, field, e);
        } finally {
            if (rjedis != null) {
                readJedisPoolManager.returnJedis(rjedis);
            }
        }
        return ret;
    }

    public Map<String, String> hgetAll(String key) {
        Map<String, String> ret = null;
        Jedis rjedis = null;
        try {
            rjedis = readJedisPoolManager.getJedis();
            ret = rjedis.hgetAll(key);
        } catch (Exception e) {
            LOG.error("hget from jedis error. key:{}", key, e);
            ret = new HashMap<>();
        } finally {
            if (rjedis != null) {
                readJedisPoolManager.returnJedis(rjedis);
            }
        }
        return ret;
    }

    public List<String> hvals(String key){
        List<String> vals = null;
        Jedis rjedis = null;
        try {
            rjedis = readJedisPoolManager.getJedis();
            vals = rjedis.hvals(key);
        } catch (Exception e) {
            LOG.error("hvals from jedis error. key:{}", key);
        } finally {
            if (rjedis != null) {
                readJedisPoolManager.returnJedis(rjedis);
            }
        }
        return vals;
    }

    public Set<String> hgetAllKeysByKey(String key) {
        Set<String> ret = null;
        Jedis rjedis = null;
        try {
            rjedis = readJedisPoolManager.getJedis();
            ret = rjedis.hkeys(key);
        } catch (Exception e) {
            LOG.error("hreadAllByKey from jedis error. key:{}", key);
        } finally {
            if (rjedis != null) {
                readJedisPoolManager.returnJedis(rjedis);
            }
        }
        return ret;
    }

    public Map<String, String> hgetAllBykey(String key) {
        Map<String, String> ret = null;
        Jedis rjedis = null;
        try {
            rjedis = readJedisPoolManager.getJedis();
            ret = rjedis.hgetAll(key);
        } catch (Exception e) {
            LOG.error("hreadAllByKey from jedis error. key:{}", key);
        } finally {
            if (rjedis != null) {
                readJedisPoolManager.returnJedis(rjedis);
            }
        }
        return ret;
    }

    public void hwrite(String key, String field, String value) {
        Jedis wjedis = null;
        try {
            wjedis = writeJedisPoolManager.getJedis();
            wjedis.hset(key, field, value);
        } catch (Exception e) {
            LOG.error("hwrite from jedis error. key:{} field:{} value:{}", key, field, value);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
    }


    public void hdelete(String key, String field, String value) {
        Jedis wjedis = null;
        try {
            wjedis = writeJedisPoolManager.getJedis();
            wjedis.hdel(key, field);
        } catch (Exception e) {
            LOG.error("hdelete from jedis error. key:{} field:{} value:{}", key, field, value);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
    }

    public void hdel(String key, String field) {
        Jedis wjedis = null;
        try {
            wjedis = writeJedisPoolManager.getJedis();
            wjedis.hdel(key, field);
        } catch (Exception e) {
            LOG.error("hdel from jedis error. key:{} field:{} value:{}", key, field);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
    }

    public void hdel(String key, String... fields) {
        Jedis wjedis = null;
        try {
            wjedis = writeJedisPoolManager.getJedis();
            wjedis.hdel(key, fields);
        } catch (Exception e) {
            LOG.error("hdel from jedis error. key:{} field:{} value:{}", key, fields);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
    }

    public Long hincr(String key, String field) {
        return hincrBy(key, field, 1L);
    }

    public Long hincrBy(String key, String field, Long value) {
        Jedis wjedis = null;
        Long result = null;
        try {
            wjedis = writeJedisPoolManager.getJedis();
            result = wjedis.hincrBy(key, field, value);
        } catch (Exception e) {
            LOG.error("hincrBy from jedis error. key:{} field:{} value:{}", key, field, value);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }

        return result;
    }

    public void hdeleteKey(String key) {
        Jedis wjedis = null;
        try {
            wjedis = writeJedisPoolManager.getJedis();
            wjedis.del(key);
        } catch (Exception e) {
            LOG.error("delete from jedis error. key:{} field:{} value:{}", key);
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
    }

    public void hwrite(String key, Map<String, String> value) {
        Jedis wjedis = null;
        try {
            wjedis = writeJedisPoolManager.getJedis();
            wjedis.hmset(key, value);
        } catch (Exception e) {
            LOG.error("hwrite from jedis error. key:{} value:{}", key, value);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
    }

    /**
     * hmset 添加多条数据
     * 删除key,重新设值,并且设置过期时间
     * @param key
     * @param map
     * @param saveDate　单位为秒
     */
    public void delAndHwriteAndExpire(String key, Map<String, String> map,int saveDate) {
        Jedis wjedis = null;
        try {
            wjedis = this.writeJedisPoolManager.getJedis();
            Pipeline p = wjedis.pipelined();
            p.del(key);
            p.hmset(key,map);
            p.expire(key,saveDate);
            p.sync();
        } catch (Exception e) {
            LOG.error("delAndHwriteAndExpire from jedis error. key:{} value:{} member:{} saveDate:{}", key,map,saveDate);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
    }

    public void disableCache(String key) {
        Jedis wjedis = null;
        try {
            wjedis = writeJedisPoolManager.getJedis();
            wjedis.expire(key, 0);
        } catch (Exception e) {
            LOG.error("disableCache error. key:{} msg:{}", key, e);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
    }

    public void remove(String key) throws Exception {
        Jedis wjedis = null;
        try {
            wjedis = writeJedisPoolManager.getJedis();
            wjedis.del(key);
        } catch (Exception e) {
            LOG.error("remove error. key:{} msg:{}", key, e);
            throw new Exception("Failed to remove key " + key, e);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
    }

    public void hincrbyfloat(String key, String property, double value) {
        Jedis wjedis = null;
        try {
            wjedis = writeJedisPoolManager.getJedis();
            wjedis.hincrByFloat(key, property, value);
        } catch (Exception e) {
            LOG.error("hincrbyfloat from jedis error. key:=" + key + "&value=" + value, e);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
    }

    public Double zincrby(String key, Double score, String member) {
        Jedis wjedis = null;
        Double totalScore = new Double(0);
        try {
            wjedis = this.writeJedisPoolManager.getJedis();
            totalScore = wjedis.zincrby(key, score, member);
        } catch (Exception e) {
            LOG.error("zincrby from jedis error. key:{} score:{} member:{}", key, score, member);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }

        return totalScore;
    }

    public void zadd(String key, Double score, String member) {
        Jedis wjedis = null;
        try {
            wjedis = this.writeJedisPoolManager.getJedis();
            wjedis.zadd(key, score, member);
        } catch (Exception e) {
            LOG.error("zincrby from jedis error. key:{} score:{} member:{}", key, score, member);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
    }

    public Long zrem(String key, String member) {
        Jedis wjedis = null;
        Long amount = 0L;
        try {
            wjedis = this.writeJedisPoolManager.getJedis();
            amount = wjedis.zrem(key, member);
        } catch (Exception e) {
            LOG.error("zrem from jedis error. key:{} , member:{}", key, member);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
        return amount;
    }

    public Long zremrangeByScore(String key, double start, double end) {
        Jedis wjedis = null;
        Long amount = 0L;
        try {
            wjedis = this.writeJedisPoolManager.getJedis();
            amount = wjedis.zremrangeByScore(key, start, end);
        } catch (Exception e) {
            LOG.error("zremrangeByScore from jedis error. key:{} , start:{} , end:{}", key, start, end);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
        return amount;
    }

    public Long zremrangeByRank(String key, long start, long end) {
        Jedis wjedis = null;
        Long amount = 0L;
        try {
            wjedis = this.writeJedisPoolManager.getJedis();
            amount = wjedis.zremrangeByRank(key, start, end);
        } catch (Exception e) {
            LOG.error("zremrangeByRank from jedis error. key:{} , start:{} , end:{}", key, start, end);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
        return amount;
    }

    public Set<Map<String, Object>> zrevrange(String key, Long begin, Long end) {
        Jedis rjedis = null;
        Set<Map<String, Object>> set = Sets.newLinkedHashSet();
        try {
            rjedis = this.readJedisPoolManager.getJedis();
            Set<Tuple> tuples = rjedis.zrevrangeWithScores(key, begin, end);
            for (Tuple tuple : tuples) {
                Map<String, Object> map = Maps.newHashMap();
                map.put("member", tuple.getElement());
                map.put("score", tuple.getScore());
                set.add(map);
            }
        } catch (Exception e) {
            LOG.error("zrevrange from redis error. key:{}, begin:{}, end:{}", key, begin, end, e);
        } finally {
            if (rjedis != null) {
                this.readJedisPoolManager.returnJedis(rjedis);
            }
        }

        return set;
    }

    public Set<String> zrevrangeVals(String key, Long begin, Long end) {
        Jedis rjedis = null;
        Set<String> set =null;
        try {
            rjedis = this.readJedisPoolManager.getJedis();
            set = rjedis.zrevrange(key, begin, end);
        } catch (Exception e) {
            LOG.error("zrevrange from redis error. key:{}, begin:{}, end:{}", key, begin, end, e);
        } finally {
            if (rjedis != null) {
                this.readJedisPoolManager.returnJedis(rjedis);
            }
        }

        return set;
    }

    /**
     * 获取sorted set指定区间数据，按score从低到高
     *
     * @param key
     * @param begin
     * @param end
     * @return
     */
    public Set<Map<String, Object>> zrange(String key, Integer begin, Integer end) {
        Jedis rjedis = null;
        Set<Map<String, Object>> set = Sets.newLinkedHashSet();
        try {
            rjedis = this.readJedisPoolManager.getJedis();
            Set<Tuple> tuples = rjedis.zrangeWithScores(key, begin, end);
            for (Tuple tuple : tuples) {
                Map<String, Object> map = Maps.newHashMap();
                map.put("member", tuple.getElement());
                map.put("score", tuple.getScore());
                set.add(map);
            }
        } catch (Exception e) {
            LOG.error("zrange from redis error. key:{}, begin:{}, end:{}", key, begin, end, e);
        } finally {
            if (rjedis != null) {
                this.readJedisPoolManager.returnJedis(rjedis);
            }
        }

        return set;
    }

    /**
     * 获取sorted set中成员排名，按score从低到高
     *
     * @param key
     * @param member
     * @return
     */
    public Long zrank(String key, String member) {
        Jedis rjedis = null;
        Long ranking = null;
        try {
            rjedis = this.readJedisPoolManager.getJedis();
            ranking = rjedis.zrank(key, member);
        } catch (Exception e) {
            LOG.error("zrank from redis error. key:{}, member:{}", key, member, e);
        } finally {
            if (rjedis != null) {
                this.readJedisPoolManager.returnJedis(rjedis);
            }
        }

        return ranking;
    }

    /**
     * 获取sorted set中成员排名，从高到低
     *
     * @param key
     * @param member
     * @return
     */
    public Long zrevrank(String key, String member) {
        Jedis rjedis = null;
        Long ranking = null;
        try {
            rjedis = this.readJedisPoolManager.getJedis();
            ranking = rjedis.zrevrank(key, member);
        } catch (Exception e) {
            LOG.error("zrevrank from redis error. key:{}, member:{}", key, member, e);
        } finally {
            if (rjedis != null) {
                this.readJedisPoolManager.returnJedis(rjedis);
            }
        }

        return ranking;
    }

    /**
     * 获取sorted set中成员score
     *
     * @param key
     * @param member
     * @return
     */
    public Double zscore(String key, String member) {
        Jedis rjedis = null;
        Double ranking = new Double(0);
        try {
            rjedis = this.readJedisPoolManager.getJedis();
            ranking = rjedis.zscore(key, member);
        } catch (Exception e) {
            LOG.error("zrevrank from redis error. key:{}, member:{}", key, member, e);
        } finally {
            if (rjedis != null) {
                this.readJedisPoolManager.returnJedis(rjedis);
            }
        }

        return ranking;
    }

    /**
     * 获取sorted set中成员score
     *
     * @param key
     * @return
     */
    public Long zcard(String key) {
        Jedis rjedis = null;
        Long size = 0L;
        try {
            rjedis = this.readJedisPoolManager.getJedis();
            size = rjedis.zcard(key);
        } catch (Exception e) {
            LOG.error("zcard from redis error. key:{}, member:{}", key, e);
        } finally {
            if (rjedis != null) {
                this.readJedisPoolManager.returnJedis(rjedis);
            }
        }

        return size;
    }

    /**
     * 设置key过期时间
     *
     * @param key
     * @param seconds
     * @return
     */
    public Boolean expire(String key, Integer seconds) {
        Jedis wjedis = null;
        Long size = 0L;
        try {
            wjedis = this.writeJedisPoolManager.getJedis();
            Long result = wjedis.expire(key, seconds);

            return result == 1 ? true : false;
        } catch (Exception e) {
            LOG.error("expire from redis error. key:{}, member:{}", key, e);
        } finally {
            if (wjedis != null) {
                this.writeJedisPoolManager.returnJedis(wjedis);
            }
        }

        return false;
    }

    /**
     * 设置key过期时间
     *
     * @param key
     * @param seconds 到期时间
     * @return
     */
    public Boolean expireAt(String key, Long seconds) {
        Jedis wjedis = null;
        Long size = 0L;
        try {
            wjedis = this.writeJedisPoolManager.getJedis();
            Long result = wjedis.expireAt(key, seconds);

            return result == 1 ? true : false;
        } catch (Exception e) {
            LOG.error("expire from redis error. key:{}, member:{}", key, e);
        } finally {
            if (wjedis != null) {
                this.writeJedisPoolManager.returnJedis(wjedis);
            }
        }

        return false;
    }

    /**
     * 设置key过期时间
     *
     * @param key
     * @param millis 到期时间
     * @return
     */
    public Boolean pexpireAt(String key, Long millis) {
        Jedis wjedis = null;
        Long size = 0L;
        try {
            wjedis = this.writeJedisPoolManager.getJedis();
            Long result = wjedis.pexpireAt(key, millis);

            return result == 1 ? true : false;
        } catch (Exception e) {
            LOG.error("expire from redis error. key:{}, member:{}", key, e);
        } finally {
            if (wjedis != null) {
                this.writeJedisPoolManager.returnJedis(wjedis);
            }
        }

        return false;
    }

    /**
     * 获取sorted set指定区间member数据
     *
     * @param key
     * @param begin
     * @param end
     * @return
     */
    public Set<String> zrangeMembers(String key, Integer begin, Integer end) {
        Jedis rjedis = null;
        try {
            rjedis = this.readJedisPoolManager.getJedis();
            Set<String> sets = rjedis.zrange(key, begin, end);
            return sets;
        } catch (Exception e) {
            LOG.error("zrange from redis error. key:{}, begin:{}, end:{}", key, begin, end, e);
        } finally {
            if (rjedis != null) {
                this.readJedisPoolManager.returnJedis(rjedis);
            }
        }
        return null;
    }


    /**
     * 添加sorted数据 同时限制大小
     *
     * @param key
     * @param score
     * @param member
     * @param limitSize
     */
    public void zaddLimit(String key, double score, String member, int limitSize) {
        Jedis rjedis = null;
        try {
            rjedis = this.readJedisPoolManager.getJedis();
            Pipeline p = rjedis.pipelined();
            p.zadd(key, score, member);
            p.zremrangeByRank(key, limitSize, -1);
            p.sync();
        } catch (Exception e) {
            LOG.error("zaddLimit from redis error. key:{},e{}", e);
        } finally {
            if (rjedis != null) {
                this.readJedisPoolManager.returnJedis(rjedis);
            }
        }
    }

    /**
     * 添加sorted数据 同时限制大小
     *
     * @param key
     * @param map
     * @param limitSize
     */
    public void zaddLimit(String key, Map<String, Double> map, int limitSize) {
        Jedis rjedis = null;
        try {
            rjedis = this.readJedisPoolManager.getJedis();
            Pipeline p = rjedis.pipelined();
            p.zadd(key, map);
            p.zremrangeByRank(key, limitSize, -1);
            p.sync();
        } catch (Exception e) {
            LOG.error("zaddLimit from redis error. key:{},e{}", e);
        } finally {
            if (rjedis != null) {
                this.readJedisPoolManager.returnJedis(rjedis);
            }
        }
    }

    /**
     * zset 添加多条数据
     */
    public void zadd(String key, Map<String, Double> map, boolean clean) {
        Jedis wjedis = null;
        try {
            wjedis = this.writeJedisPoolManager.getJedis();
            Pipeline p = wjedis.pipelined();
            if (clean) {
                p.zremrangeByRank(key, 0, -1);
            }
            p.zadd(key, map);
            p.sync();
        } catch (Exception e) {
            LOG.error("zincrby from jedis error. key:{} score:{} member:{}", key);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
    }


    /**
     * 通过redis的Pipeline 批量添加list数据
     */
    public boolean lpushList(String key, List<?> list) {
        Jedis wjedis = null;
        boolean status = false;
        try {
            wjedis = this.writeJedisPoolManager.getJedis();
            Pipeline p = wjedis.pipelined();
            if (list != null && !list.isEmpty()) {
                for (Object el : list) {
                    p.lpush(key, String.valueOf(el));
                }
            }
            p.sync();
            status = true;
            return status;
        } catch (Exception e) {
            LOG.error("lpushList from jedis error. key:{}  list:{}", key, list);
            return status;
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }

        }
    }

    /**
     * 通过redis的Pipeline 批量添加Set数据
     */
    public boolean saddSet(String key, List<?> list) {
        Jedis wjedis = null;
        boolean status = false;
        try {
            wjedis = this.writeJedisPoolManager.getJedis();
            if (list != null && !list.isEmpty()) {
                // 去重
                Set<?> set = new HashSet<>(list);
                Pipeline p = wjedis.pipelined();
                for (Object el : set) {
                    p.sadd(key, String.valueOf(el));
                }
                p.sync();
            }
            status = true;
            return status;
        } catch (Exception e) {
            LOG.error("lpushList from jedis error. key:{}  list:{}", key, list);
            return status;
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }

        }
    }
    /**
     * 返回指定权重区间的元素集合
     *
     * @param key
     * @param min 上限权重
     * @param max 下限权重z
     * @return Set<String>
     */
    public Set<String> zrangeByScore(String key, String min, String max) {
        Jedis rjedis = null;
        try {
            rjedis = readJedisPoolManager.getJedis();
            Set<String> set = rjedis.zrangeByScore(key, min, max);
            return set;
        } catch (Exception e) {
            LOG.error("zrangeByScore from jedis error. key:{} min:{} max:{}", key, min, max);
        } finally {
            if (rjedis != null) {
                readJedisPoolManager.returnJedis(rjedis);
            }
        }
        return null;
    }


    public Set<String> smembers(String key) {
        Jedis rjedis = null;
        Set<String> result = null;
        try {
            rjedis = readJedisPoolManager.getJedis();
            result = rjedis.smembers(key);
            return result;
        } catch (Exception e) {
            LOG.error("llen from jedis error. key:{}", key);
        } finally {
            if (rjedis != null) {
                readJedisPoolManager.returnJedis(rjedis);
            }
        }
        return null;
    }

    /**
     * 执行脚本
     */
    public Object eval(String script, int keyCount, String... params) {
        Jedis wjedis = null;
        Object result = null;
        try {
            wjedis = this.writeJedisPoolManager.getJedis();
            result = wjedis.eval(script, keyCount, params);
        } catch (Exception e) {
            LOG.error("eval from jedis error  params:{},e:{}", params,e);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }

        }
        return result;
    }

    /**
     * 确定一个给定的值是否存在
     *
     * @param key
     * @param member
     * @return
     */
    public boolean sismember(String key, String member) {
        Jedis rjedis = null;
        try {
            rjedis = readJedisPoolManager.getJedis();
            boolean s = rjedis.sismember(key, member);
            return s;
        } catch (Exception e) {
            LOG.error("sismember from jedis error. key:{} member:{}", key, member);
        } finally {
            if (rjedis != null) {
                readJedisPoolManager.returnJedis(rjedis);
            }
        }
        return false;
    }

    /**
     * 向Set添加一条记录，如果member已存在返回0,否则返回1
     *
     * @param key
     * @param member
     * @return
     */
    public long sadd(String key, String member) {
        Jedis wjedis = null;
        try {
            wjedis = writeJedisPoolManager.getJedis();
            return wjedis.sadd(key, member);
        } catch (Exception e) {
            LOG.error("sadd from jedis error. key:{}", key);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
        return 0;
    }

    /**
     * 检测key是否存在
     *
     * @param key
     * @return
     */
    public Boolean exits(String key) {
        Jedis wjedis = null;
        try {
            wjedis = this.writeJedisPoolManager.getJedis();
            return wjedis.exists(key);
        } catch (Exception e) {
            LOG.error("expire from redis error. key:{}, member:{}", key, e);
        } finally {
            if (wjedis != null) {
                this.writeJedisPoolManager.returnJedis(wjedis);
            }
        }
        return false;
    }

    /**
     * 从集合中删除指定成员
     *
     * @param key
     * @param member
     * @return
     */
    public Long srem(String key, String member) {
        Jedis wjedis = null;
        try {
            wjedis = writeJedisPoolManager.getJedis();
            return wjedis.srem(key, member);
        } catch (Exception e) {
            LOG.error("srem from jedis error. key:{}", key);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
        return 0L;
    }

    /**
     * 批量删除成员集合
     * @param key
     * @param list
     * @return
     */
    public boolean sremList(String key, List<?> list) {
        Jedis wjedis = null;
        boolean status = false;
        try {
            wjedis = this.writeJedisPoolManager.getJedis();
            if (list != null && !list.isEmpty()) {
                // 去重
                Set<?> set = new HashSet<>(list);
                Pipeline p = wjedis.pipelined();
                for (Object el : set) {
                    p.srem(key, String.valueOf(el));
                }
                p.sync();
            }
            status = true;
            return status;
        } catch (Exception e) {
            LOG.error("sremList from jedis error. key:{}  list:{}", key, list);
            return status;
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }

        }
    }
    /**
     * 批量删除redisKey
     * @param key
     * @throws Exception
     */
    public void batchRemove(String key) throws Exception {
        Jedis wjedis = null;
        try {
            wjedis = writeJedisPoolManager.getJedis();
            Set<String> set = wjedis.keys(key + "*");
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String keyStr = it.next();
                wjedis.del(keyStr);
            }
        } catch (Exception e) {
            LOG.error("batchRemove error. key:{} msg:{}", key, e);
            throw new Exception("Failed to batchRemove key " + key, e);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
    }

    /**
     * 批量删除key
     * @param keys
     * @throws Exception
     */
    public void batchRemoveKey(String... keys) throws Exception {
        Jedis wjedis = null;
        try {
            if(keys.length>0) {
                wjedis = writeJedisPoolManager.getJedis();
                Pipeline p = wjedis.pipelined();
                for (String key : keys) {
                    p.del(key);
                }
                p.sync();
            }
        } catch (Exception e) {
            LOG.error("batchRemoveKey error. key:{}", keys);
            throw new Exception("Failed to batchRemoveKey key " + keys);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
    }

    /**
     * 执行lua脚本
     *
     * @param scriptSha
     * @param keys
     * @param args
     * @return
     */
    public Object evalsha(String scriptSha, List<String> keys, List<String> args) {
        Jedis wjedis = null;
        try {
            wjedis = writeJedisPoolManager.getJedis();
            return wjedis.evalsha(scriptSha, keys, args);
        } catch (Exception e) {
            LOG.error("evalScript fail.", e);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
        return null;
    }

    /**
     * 加载lua脚本
     *
     * @param script
     * @return
     */
    public String scriptLoad(String script) {
        Jedis wjedis = null;
        try {
            wjedis = writeJedisPoolManager.getJedis();
            return wjedis.scriptLoad(script);
        } catch (Exception e) {
            LOG.error("scriptLoad fail.", e);
        } finally {
            if (wjedis != null) {
                writeJedisPoolManager.returnJedis(wjedis);
            }
        }
        return null;
    }


    public Long hlen(String key) {
        Jedis rjedis = null;
        try {
            rjedis = readJedisPoolManager.getJedis();
            return rjedis.hlen(key);
        } catch (Exception e) {
            LOG.error("hlen from jedis error. key:{}", key);
        } finally {
            if (rjedis != null) {
                readJedisPoolManager.returnJedis(rjedis);
            }
        }
        return null;
    }

    public void rename(String key, String newKey) {
        Jedis rjedis = null;
        try {
            rjedis = readJedisPoolManager.getJedis();
            rjedis.rename(key, newKey);
        } catch (Exception e) {
            LOG.error("rename from jedis error. key:{}, newkey:{}, error:{}", key, newKey, e);
        } finally {
            if (rjedis != null) {
                readJedisPoolManager.returnJedis(rjedis);
            }
        }
    }




    /**
     * @description 随机获取一个元素
     * @author
     */
    public List<String> srandmember(String key, int count) {
        Jedis rjedis = null;
        try {
            rjedis = readJedisPoolManager.getJedis();
            return rjedis.srandmember(key, count);
        } catch (Exception e) {
            LOG.error("srandmember from jedis error. ", e);
        } finally {
            if (rjedis != null) {
                readJedisPoolManager.returnJedis(rjedis);
            }
        }
        return null;
    }

    /**
     * @description 随机删除一个元素，并返回。线上redis不支持这个方法
     * @author
     * @return
     */
    public Set<String> spop(String key, int count) {
        Jedis rjedis = null;
        try {
            rjedis = readJedisPoolManager.getJedis();
            return rjedis.spop(key, count);
        } catch (Exception e) {
            LOG.error("spop from jedis error. ", e);
        } finally {
            if (rjedis != null) {
                readJedisPoolManager.returnJedis(rjedis);
            }
        }
        return null;
    }

    /**
     * 发布消息
     * @param channel
     * @param message
     */
    public void publish(String channel ,String message) {
        Jedis rjedis = null;
        try {
            rjedis = readJedisPoolManager.getJedis();
            rjedis.publish(channel, message);
        } catch (Exception e) {
            LOG.error("publish from jedis error. channel:{}, messge:{}, error:{}", channel, message, e);
        } finally {
            if (rjedis != null) {
                readJedisPoolManager.returnJedis(rjedis);
            }
        }
    }

    /**
     * 订阅消息
     * @param channel
     * @param jedisPubSub
     */
    public void subscribe(String channel, JedisPubSub jedisPubSub) {
        Jedis rjedis = null;
        try {
            rjedis = readJedisPoolManager.getJedis();
            rjedis.subscribe(jedisPubSub, channel);
        } catch (Exception e) {
            LOG.error("subscribe from jedis error. channel:{}, jedisPubSub, error:{}", channel, e);
        } finally {
            if (rjedis != null) {
                readJedisPoolManager.returnJedis(rjedis);
            }
        }
    }
}
