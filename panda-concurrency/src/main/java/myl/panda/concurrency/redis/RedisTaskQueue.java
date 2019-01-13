package myl.panda.concurrency.redis;

import myl.panda.concurrency.BaseTaskFactory;
import myl.panda.concurrency.pools.TaskPool;
import myl.panda.concurrency.queues.MonitorTaskQueue;
import myl.panda.concurrency.tasks.AbstractTask;
import myl.panda.utils.RedisLockUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;

/**
 * create by maoyule on 2019/1/12
 */
public class RedisTaskQueue extends AbstractTask {
    private RedisLock lock;
    private String listKey;
    private StringRedisTemplate template;
    private MonitorTaskQueue taskQueue;

    public RedisTaskQueue(String listKey){
        this(BaseTaskFactory.getFactory().getMainPool(), listKey);
    }

    public RedisTaskQueue(TaskPool taskPool, String listKey) {
        Assert.hasLength(listKey, "list key is null");
        this.taskQueue = new MonitorTaskQueue(taskPool);
        this.listKey = listKey;
        this.template = RedisLockUtils.getRedisTemplate();
        Assert.notNull(template, "redis template is null");
        this.lock = new RedisLock(listKey + "_lock");
    }

    public void add(String event) {
        this.template.opsForList().rightPush(listKey, event);
        this.taskQueue.add(this);
    }

    protected void execute() {
        while(true){
            String event = this.template.opsForList().leftPop(listKey);
        }
    }

    @Override
    public void run() {
        execute();
    }
}
