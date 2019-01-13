package myl.panda.timers;


import myl.panda.concurrency.BaseTaskFactory;
import myl.panda.concurrency.queues.TaskQueue;
import myl.panda.concurrency.tasks.AbstractTask;

/**
 * Created by maoyule on 2019/1/9.
 */
public abstract class SecondTask extends AbstractTask {
    public SecondTask(TaskQueue queue){
        this.queue = queue;
    }

    /**
     * 触发tick，由timerMgr调取
     */
    public void attach(){
        if(this.queue != null){
            this.queue.add(this);
        }else{
            BaseTaskFactory.getFactory().addSlow(this);
        }
    }
}
