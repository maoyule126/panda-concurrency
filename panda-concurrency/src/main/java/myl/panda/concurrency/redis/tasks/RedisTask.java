package myl.panda.concurrency.redis.tasks;

import myl.panda.concurrency.tasks.AbstractTask;

/**
 * create by maoyule on 2019/2/23
 */
public abstract class RedisTask extends AbstractTask {
    private int taskId;

    public RedisTask(){
        taskId = -1;
    }

    public RedisTask(int taskId){
        this.taskId = taskId;
    }

    public int getTaskId() {
        return taskId;
    }
}
