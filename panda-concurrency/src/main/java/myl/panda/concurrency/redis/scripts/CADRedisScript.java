package myl.panda.concurrency.redis.scripts;

import org.springframework.data.redis.core.script.RedisScript;

/**
 * create by maoyule on 2019/1/12
 */
public class CADRedisScript implements RedisScript<Boolean> {

    @Override
    public String getSha1() {
        return "cas_and_del";
    }

    @Override
    public Class<Boolean> getResultType() {
        return Boolean.class;
    }

    @Override
    public String getScriptAsString() {
        return "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
    }
}
