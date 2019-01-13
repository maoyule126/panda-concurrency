package myl.panda.concurrency.tasks;


import myl.panda.concurrency.queues.ITaskQueue;

/**
 * create by maoyule on 2018/1/8
 */
public interface ITask extends Runnable {
    /**
     * 任务接受者
     *
     * @param queue
     */
    void setQueue(ITaskQueue queue);

    /**
     * 任务接受者
     *
     * @return
     */
    ITaskQueue getQueue();

    @Override
    void run();
}
