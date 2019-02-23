package myl.panda.concurrency.redis.queues;

import myl.panda.concurrency.BaseTaskFactory;
import myl.panda.concurrency.pools.TaskPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * create by maoyule on 2019/2/23
 */
public abstract class RedisStrongTaskQueue extends RedisMiddleTaskQueue {
    private static final Logger logger = LoggerFactory.getLogger(RedisStrongTaskQueue.class);

    public RedisStrongTaskQueue(String listKey) {
        super(BaseTaskFactory.getFactory().getMainPool(), listKey);
    }

    public RedisStrongTaskQueue(TaskPool taskPool, String listKey) {
        super(taskPool, listKey);
    }

    @Override
    protected void doRun() {
        try {
            addAllTasksToRedis();
            boolean isLock = lock.tryLock();
            if (isLock) {
                try {
                    runAllRedisTasks();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    lock.unlock();
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }
}
