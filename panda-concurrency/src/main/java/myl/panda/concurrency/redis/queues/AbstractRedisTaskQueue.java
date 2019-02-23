package myl.panda.concurrency.redis.queues;

import myl.panda.concurrency.BaseTaskFactory;
import myl.panda.concurrency.clouds.ICloudLock;
import myl.panda.concurrency.pools.TaskPool;
import myl.panda.concurrency.queues.AbstractTaskQueue;
import myl.panda.utils.RedisLockUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * create by maoyule on 2019/2/23
 */
public abstract class AbstractRedisTaskQueue extends AbstractTaskQueue {
    protected ICloudLock lock;
    protected String listKey;
    protected StringRedisTemplate template;
    protected boolean isRunning = true;

    public AbstractRedisTaskQueue(String listKey) {
        this(BaseTaskFactory.getFactory().getMainPool(), listKey);
    }

    public AbstractRedisTaskQueue(TaskPool taskPool, String listKey) {
        super(taskPool);
        this.listKey = listKey;
        this.template = RedisLockUtils.getRedisTemplate();
        Assert.notNull(template, "redis template is null");
    }

    @Override
    protected void preInit() {
        this.list = new ConcurrentLinkedQueue<>();
    }
}
