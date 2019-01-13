package myl.panda.concurrency.redis;

import myl.panda.concurrency.tasks.ITask;

/**
 * create by maoyule on 2019/1/12
 */
public interface IRedisTask extends ITask {
    String getRedisEvent();
    void execute(String redisEvent);
}
