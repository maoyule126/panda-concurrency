package myl.panda.concurrency.tasks;

import myl.panda.concurrency.queues.ITaskQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public abstract class AbstractTask implements ITask {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractTask.class);

    protected ITaskQueue queue;

    public AbstractTask() {
    }

    @Override
    public void setQueue(ITaskQueue queue) {
        this.queue = queue;
    }

    @Override
    public ITaskQueue getQueue() {
        return queue;
    }

    @Override
    public void run() {
        try {
            execute();
        }catch (Exception e){
            logger.error("{}, {}", this.toString(), e.getMessage(), e);
        }
    }

    protected abstract void execute();
}
