package myl.panda.concurrency.redis.queues;

import myl.panda.concurrency.pools.TaskPool;
import myl.panda.concurrency.tasks.ITask;
import myl.panda.timers.DelayTask;
import myl.panda.timers.TimerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * create by maoyule on 2019/2/23
 */
public class RedisSimpleTaskQueue extends AbstractRedisTaskQueue {
    protected static final long RUN_INTERVAL_LIMIT = 5000L;
    private static final Duration DEFAULT_DELAY_DURATION = Duration.ofSeconds(5);

    private static final Logger logger = LoggerFactory.getLogger(RedisSimpleTaskQueue.class);

    private Duration delayDuration;
    private DelayTask delayTask;

    public RedisSimpleTaskQueue(String listKey) {
        super(listKey);
        this.delayDuration = DEFAULT_DELAY_DURATION;
    }

    public RedisSimpleTaskQueue(String listKey, Duration delayDuration) {
        super(listKey);
        this.delayDuration = delayDuration;
    }

    public RedisSimpleTaskQueue(TaskPool taskPool, String listKey, Duration delayDuration) {
        super(taskPool, listKey);
        this.delayDuration = delayDuration;
    }

    @Override
    protected void doRun() {
        if(delayTask != null){
            delayTask.cancel();
            delayTask = null;
        }
        boolean isLock = lock.tryLock();
        if (isLock) {
            try{
                doRun0();
            }catch (Exception e){
                logger.error(e.getMessage(), e);
            }finally {
                lock.unlock();
            }
        }else{
            delayTask = new DelayTask(this, (int)delayDuration.toMillis()) {
                @Override
                protected void execute() {
                    doRun();
                }
            };
            TimerService.getService().addDelay(delayTask);
        }
    }

    private void doRun0(){
        long startTime = System.currentTimeMillis();
        while (isRunning) {
            ITask task = list.poll();
            if (task == null) {
                break;
            }
            try {
                task.run();
                long tempTime = System.currentTimeMillis();
                if (tempTime - startTime > RUN_INTERVAL_LIMIT) {
                    isInterrupt = true;
                    break;
                }
            } catch (Exception e) {
                logger.error("{}, {}", this.toString(), e.getMessage(), e);
            }
        }
    }
}
