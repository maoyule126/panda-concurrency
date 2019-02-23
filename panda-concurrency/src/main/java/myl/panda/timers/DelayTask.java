package myl.panda.timers;

import myl.panda.concurrency.queues.ITaskQueue;

/**
 * 延时执行，只执行一次
 * Created by maoyule on 2018/4/8.
 */
public abstract class DelayTask extends LoopTask {
    public DelayTask(int delayTime){
        super(null, delayTime, 1);
    }

    public DelayTask(ITaskQueue queue, int delayTime){
        super(queue, delayTime, 1);
    }
}
