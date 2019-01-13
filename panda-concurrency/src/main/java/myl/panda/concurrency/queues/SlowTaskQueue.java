package myl.panda.concurrency.queues;

import myl.panda.concurrency.BaseTaskFactory;

/**
 * create by maoyule on 2019/1/9
 */
public class SlowTaskQueue extends MonitorTaskQueue {
    public SlowTaskQueue() {
        super(BaseTaskFactory.getFactory().getMainPool());
    }

    public SlowTaskQueue(long runOnceLimit) {
        super(BaseTaskFactory.getFactory().getMainPool(), runOnceLimit);
    }
}
