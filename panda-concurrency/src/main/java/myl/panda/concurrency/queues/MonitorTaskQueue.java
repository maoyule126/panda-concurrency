package myl.panda.concurrency.queues;

import myl.panda.concurrency.pools.TaskPool;
import myl.panda.concurrency.tasks.ITask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * create by maoyule on 2019/1/9
 */
public class MonitorTaskQueue extends AbstractTaskQueue {
    protected static final long RUN_INTERVAL_LIMIT = 5000L;

    private static final Logger logger = LoggerFactory.getLogger(MonitorTaskQueue.class);

    private boolean isRunning = true;
    private long runOnceLimit;


    /**
     * 采用默认的任务管理器
     *
     * @param taskPool
     */
    public MonitorTaskQueue(TaskPool taskPool) {
        this(taskPool, 0);
    }

    /**
     * 采用默认的任务管理器
     *
     * @param taskPool
     */
    public MonitorTaskQueue(TaskPool taskPool, long runOnceLimit) {
        super(taskPool);
        this.runOnceLimit = runOnceLimit <= 0 ? RUN_INTERVAL_LIMIT : runOnceLimit;
    }

    @Override
    protected void preInit() {
        this.list = new ConcurrentLinkedQueue<>();
    }

    @Override
    protected void doRun() {
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
                    logger.warn("一次Running执行超时，打断执行: 时间：{}", (tempTime - startTime));
                    isInterrupt = true;
                    break;
                }
            } catch (Exception e) {
                logger.error("{}, {}", this.toString(), e.getMessage(), e);
            }
        }
    }

}
