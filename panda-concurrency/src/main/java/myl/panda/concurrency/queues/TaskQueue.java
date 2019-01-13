package myl.panda.concurrency.queues;

import myl.panda.concurrency.BaseTaskFactory;

/**
 * create by maoyule on 2019/1/9
 */
public class TaskQueue extends MonitorTaskQueue {
    public TaskQueue() {
        super(BaseTaskFactory.getFactory().getMainPool());
    }

    public TaskQueue(long runOnceLimit) {
        super(BaseTaskFactory.getFactory().getMainPool(), runOnceLimit);
    }
}
