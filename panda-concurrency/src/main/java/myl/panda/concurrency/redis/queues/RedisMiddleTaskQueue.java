package myl.panda.concurrency.redis.queues;

import com.alibaba.fastjson.JSON;
import myl.panda.concurrency.BaseTaskFactory;
import myl.panda.concurrency.clouds.ICloudLock;
import myl.panda.concurrency.pools.TaskPool;
import myl.panda.concurrency.queues.AbstractTaskQueue;
import myl.panda.concurrency.redis.RedisLock;
import myl.panda.concurrency.redis.tasks.RedisTask;
import myl.panda.utils.RedisLockUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;

import java.util.concurrent.TimeUnit;

/**
 * create by maoyule on 2019/1/12
 */
public abstract class RedisMiddleTaskQueue extends AbstractRedisTaskQueue {
    private static final long EXPIRE_TIME = 30 * 60;
    private static final long LOCAL_RUN_ONCE_TIMES = 5000L;

    private static final Logger logger = LoggerFactory.getLogger(Logger.class);

    public RedisMiddleTaskQueue(String listKey) {
        this(BaseTaskFactory.getFactory().getMainPool(), listKey);
    }

    public RedisMiddleTaskQueue(TaskPool taskPool, String listKey) {
        super(taskPool, listKey);
    }

    @Override
    protected void preInit() {
        super.preInit();
        this.lock = new RedisLock(listKey + "_lock");
    }

    @Override
    protected void doRun() {
        try {
            boolean isLock = lock.tryLock();
            if (isLock) {
                try {
                    String taskStr = this.template.opsForList().leftPop(listKey);
                    if (taskStr == null) {
                        runAllLocalTasks();
                    } else {
                        RedisTask remoteTask = decodeTask(taskStr);
                        remoteTask.run();
                        addAllTasksToRedis();
                        runAllRedisTasks();
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    lock.unlock();
                }
            } else {
                addAllTasksToRedis();
                isLock = lock.tryLock();
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

    private void runAllLocalTasks() {
        long startTime = System.currentTimeMillis();
        while (isRunning) {
            RedisTask task = (RedisTask) list.poll();
            if (task == null) {
                break;
            }
            task.run();
            long costTime = System.currentTimeMillis() - startTime;
            if (costTime - startTime > LOCAL_RUN_ONCE_TIMES) {
                break;
            }
        }
    }

    protected void runAllRedisTasks() {
        while (isRunning) {
            String taskStr = this.template.opsForList().leftPop(listKey);
            if (taskStr == null) {
                break;
            }
            RedisTask remoteTask = decodeTask(taskStr);
            remoteTask.run();
        }
    }

    protected void addAllTasksToRedis() {
        this.template.expire(listKey, EXPIRE_TIME, TimeUnit.SECONDS);
        while (isRunning) {
            try {
                RedisTask task = (RedisTask) list.poll();
                if (task == null) {
                    break;
                }
                String taskStr = encodeTask(task);
                if (taskStr != null) {
                    this.template.opsForList().rightPush(listKey, taskStr);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    protected void addTaskToRedis(RedisTask task) {
        this.template.expire(listKey, EXPIRE_TIME, TimeUnit.SECONDS);
        try {
            String taskStr = encodeTask(task);
            if (taskStr != null) {
                this.template.opsForList().rightPush(listKey, taskStr);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    protected RedisTask decodeTask(String taskStr) {
        int index = taskStr.indexOf(":");
        if (index == -1) {
            logger.error("can not find taskId, taskStr:{}", taskStr);
            return null;
        }
        try {
            int taskId = Integer.parseInt(taskStr.substring(0, index));
            Class<RedisTask> taskClazz = getTaskClass(taskId);
            String taskJson = taskStr.substring(index + 1);
            return JSON.parseObject(taskJson, taskClazz);
        } catch (Exception e) {
            logger.error("task parse error. taskStr:{}", taskStr, e);
        }
        return null;
    }

    protected abstract Class<RedisTask> getTaskClass(int taskId);

    protected String encodeTask(RedisTask task) {
        try {
            String taskJson = JSON.toJSONString(task);
            return task.getTaskId() + ":" + taskJson;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
}
