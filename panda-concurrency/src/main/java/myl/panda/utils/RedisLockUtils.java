package myl.panda.utils;

import myl.panda.concurrency.redis.ConRedisTemplate;
import myl.panda.concurrency.redis.scripts.CADRedisScript;
import myl.panda.concurrency.redis.scripts.CASRedisScript;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Arrays;

/**
 * create by maoyule on 2019/1/12
 */
public class RedisLockUtils {
    /**  cas比较并设置 **/
    private static CASRedisScript casRedisScript = new CASRedisScript();
    /** cad比较并删除 **/
    private static CADRedisScript cadRedisScript = new CADRedisScript();

    private static ConRedisTemplate redisTemplate;

    private static String redisLockIdPre;

    public static void setRedisTemplate(ConRedisTemplate template){
        redisTemplate = template;
    }

    public static ConRedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    public static String getRedisLockIdPre() {
        return redisLockIdPre;
    }

    public static void setRedisLockIdPre(String redisLockIdPre) {
        RedisLockUtils.redisLockIdPre = redisLockIdPre;
    }

    /**
     * 比较并设置。如果存储值与设置一致，则设为该值，并返回true,否则，返回false
     * @param key
     * @param value
     * @return
     */
    public static boolean compareAndSet(String key, String value){
        return redisTemplate.execute(casRedisScript, Arrays.asList(key), value);
    }

    /**
     * 比较并设置。如果存储值与设置一致，则删除该key，并返回true,否则，返回false
     * @param key
     * @param value
     * @return
     */
    public static boolean compareAndDelete(String key, String value){
        return redisTemplate.execute(cadRedisScript, Arrays.asList(key), value);
    }
}
