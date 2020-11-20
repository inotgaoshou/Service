package com.service.zgbj.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JedisLockService {

    private static final Logger logger = LoggerFactory.getLogger(JedisLockService.class);

    @Autowired
    private JedisService jedisService;


    private static final String LOCK_KEY = "jedis_lock";
    /** 等待锁的时间 */
    private static final int RETRY_TIME = 5 * 1000;
    /*8 锁超时的时间 */
    private static final int EXPIRE_TIME = 5 * 1000;


    public String lock() {
        return lock(LOCK_KEY);
    }

    public String lock(String lockKey) {
        return lock(lockKey, RETRY_TIME);
    }

    public String lock(String lockKey, int waitTime) {
        return lock(lockKey, waitTime, EXPIRE_TIME);
    }

    public String lock(String lockKey, int waitTime, int timeout) {
        long start = System.currentTimeMillis();
        String result = null;
        int retryTime = waitTime;
        try {
            long lockValue = 0;
            while (retryTime > 0) {
                lockValue = System.nanoTime();
                String setResult = jedisService.set(lockKey, String.valueOf(lockValue), "NX", "PX", timeout);
                if ("OK".equalsIgnoreCase(setResult)) {
                    result = lockValue + "";
                    return result;
                }
                retryTime -= 10;
                Thread.sleep(10);
            }
        } catch (Exception e) {
            logger.error("lock error, lockKey:"+lockKey+", waitTime:"+waitTime+", timeout:"+timeout, e);
        } finally {
            logger.info("获取redis分布式锁结束，key:{}, waitTime:{}ms, timeout:{}ms, 耗时:{}ms, lockVal:{}, 结果:{}", lockKey, waitTime,
                    timeout, (System.currentTimeMillis() - start), result, (null == result ? "失败" : "成功"));
        }
        return null;
    }

    /**
     * 尝试获取锁，不会重试
     * @param lockKey
     * @param timeout
     * @return
     */
    public String lockWithoutRetry(String lockKey, int timeout) {
        long lockValue = System.nanoTime();
        String result = jedisService.set(lockKey, String.valueOf(lockValue), "NX", "PX", timeout);
        if ("OK".equalsIgnoreCase(result)) {
            return lockValue + "";
        }
        return null;
    }


    public void unlock(String lockKey, String lockVal) {
        String currLockVal = jedisService.get(lockKey);
        if (currLockVal != null && currLockVal.equals(lockVal)) {
            Long result = jedisService.del(lockKey);
            logger.info("解除redis分布式锁完成，key:{}, lockVal:{}, 解除结果：{}", lockKey, lockVal,
                    (null == result || result <= 0) ? "失败，key不存在，未获得锁或锁已过期" : "成功");
        } else {
            logger.info("解除redis分布式锁失败，lockVal不正确的，key:{}, lockVal:{}", lockKey, lockVal);
        }
    }

    public boolean isExist(String lockKey, int timeout) {
        String result = jedisService.set(lockKey, "1", "NX", "PX", timeout);
        if ("OK".equalsIgnoreCase(result)) {
            return true;
        }
        return false;
    }
}
