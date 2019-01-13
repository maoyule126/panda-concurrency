package myl.panda.concurrency.redis;

import myl.panda.utils.RedisLockUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * redis分布式锁
 * create by maoyule on 2019/1/12
 */
public class RedisLock implements Lock {
    private static final Duration DEFAULT_EXPIRE_TIME = Duration.ofSeconds(10);
    private static final long SLEEP_TIME = 20;

    private StringRedisTemplate template;
    private String key;
    private Duration expireTime;
    private String idPre;
    private String lockValue = null;
    private boolean isLock = false;

    public RedisLock(String key){
        this(key, DEFAULT_EXPIRE_TIME);
    }

    public RedisLock(String key, Duration expireTime){
        this.template = RedisLockUtils.getRedisTemplate();
        Assert.notNull(template, "redis template is null");
        Assert.hasLength(key, "key is null or empty");
        this.key = key;
        this.expireTime = expireTime;
        this.idPre = RedisLockUtils.getRedisLockIdPre();
    }

    @Override
    public void lock() {
        if(isLock){
            return;
        }
        while(true){
            lockValue = generateLockValue();
            boolean ret = template.opsForValue().setIfAbsent(key, lockValue, expireTime);
            if (ret) {
                isLock = true;
                break;
            }
            try {
                Thread.sleep(SLEEP_TIME);
            }catch (InterruptedException e){

            }
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        if(isLock){
            return;
        }
        while(true){
            String value = generateLockValue();
            boolean ret = template.opsForValue().setIfAbsent(key, value, expireTime);
            if (ret) {
                isLock = true;
                break;
            }
            Thread.sleep(SLEEP_TIME);
        }
    }

    @Override
    public boolean tryLock() {
        if(isLock){
            return true;
        }
        lockValue = generateLockValue();
        isLock = template.opsForValue().setIfAbsent(key, lockValue, expireTime);
        return isLock;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        if(isLock){
            return true;
        }
        long endTime = System.currentTimeMillis() + unit.toMillis(time);
        while(true){
            lockValue = generateLockValue();
            boolean ret = template.opsForValue().setIfAbsent(key, lockValue, expireTime);
            if (ret) {
                return true;
            }
            long remainTime = endTime - System.currentTimeMillis();
            if(remainTime < 0){
                break;
            }
            Thread.sleep(remainTime > SLEEP_TIME ? SLEEP_TIME : remainTime);
        }
        return false;
    }

    private String generateLockValue(){
        return idPre + System.currentTimeMillis();
    }

    @Override
    public void unlock(){
        if(lockValue != null){
            RedisLockUtils.compareAndDelete(key, lockValue);
            lockValue = null;
            isLock = false;
        }
    }

    public boolean isLock(){
        return isLock;
    }

    @Override
    public Condition newCondition() {
        // unrealized
        return null;
    }
}
