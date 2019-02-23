package myl.panda.concurrency.queues;

import myl.panda.concurrency.pools.TaskPool;
import myl.panda.concurrency.tasks.ITask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * create by maoyule on 2019/1/7
 */
public abstract class AbstractTaskQueue implements ITaskQueue {
    private static final Logger logger = LoggerFactory.getLogger(AbstractTaskQueue.class);

    protected Queue<ITask> list;
    private Lock lock;
    private volatile boolean isAdded;

    protected TaskPool taskPool;
    /**
     * 如果执行时间过长，就打断，isInterrupt为打断标记，使用者不要自己改变它的值
     */
    protected boolean isInterrupt;

    /**
     * 采用默认的任务管理器
     */
    public AbstractTaskQueue(TaskPool taskPool) {
        this.taskPool = taskPool;
        isAdded = false;
        this.lock = new ReentrantLock();
        preInit();
    }

    protected abstract void preInit();


    @Override
    public void run() {
        try {
            boolean isRemove = true;
            doRun();
            if (isRemove && !isInterrupt) {
                endAndRemove();
            } else {
                isInterrupt = false;
                endAndRemove();
            }
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    protected abstract void doRun();

    private void addToRun() {
        while (true) {
            boolean ret;
            try {
                ret = lock.tryLock(1L, TimeUnit.SECONDS);
                if (ret) {
                    break;
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
        try {
            if (isAdded == false) {
                try {
                    taskPool.add(this);
                    isAdded = true;
                } catch (Exception e) {
                    logger.error("", e);
                }
            }
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            lock.unlock();
        }
    }

    private void endAndRemove() {
        while (true) {
            boolean ret;
            try {
                ret = lock.tryLock(1L, TimeUnit.SECONDS);
                if (ret) {
                    break;
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
        try {
            if (isAdded == true) {
                isAdded = false;
            }
            if(!list.isEmpty()){
                logger.info("after remove list.size > 0");
                taskPool.add(this);
                isAdded = true;
            }
        } catch (Exception e) {

        } finally {
            lock.unlock();
        }
    }

    @Override
    public void add(ITask task) {
        list.add(task);
        addToRun();
    }
}
