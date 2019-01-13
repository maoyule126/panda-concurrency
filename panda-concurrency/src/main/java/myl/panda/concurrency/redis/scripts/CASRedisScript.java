package myl.panda.concurrency.redis.scripts;

import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

/**
 * create by maoyule on 2019/1/12
 */
@Component
public class CASRedisScript implements RedisScript<Boolean> {
    @Override
    public String getSha1() {
        return "redis_cas";
    }

    @Override
    public Class<Boolean> getResultType() {
        return Boolean.class;
    }

    @Override
    public String getScriptAsString() {
        return "if redis.call('get', KEYS[1]) == ARGV[1] then return 1 else return 0 end";
    }
}
