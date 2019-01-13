package myl.panda.timers;

import myl.panda.concurrency.BaseTaskFactory;
import myl.panda.concurrency.queues.TaskQueue;
import myl.panda.concurrency.tasks.AbstractTask;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * create by maoyule on 2019/1/9
 */
public abstract class LoopTask extends AbstractTask implements Delayed {
    private long delayTime;
    private long expireTime;
    private long loopCount = -1;
    /** 是否正处在触发未执行完状态 **/
    private AtomicBoolean attaching = new AtomicBoolean();

    public LoopTask(int delayTime, int loopCount){
        this(null, delayTime, loopCount);
    }

    public LoopTask(TaskQueue queue, int delayTime){
        this(null, delayTime, 0);
    }

    /**
     *
     * @param delayTime 单位 ms
     * @param loopCount 循环次数 小于等于0表示无限循环
     */
    public LoopTask(TaskQueue queue, int delayTime, int loopCount){
        this.queue = queue;
        this.delayTime = delayTime;
        this.expireTime = System.currentTimeMillis() + this.delayTime;
        this.loopCount = loopCount;
        if(this.loopCount <= 0){
            this.loopCount = -1;
        }
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(this.expireTime - System.currentTimeMillis(), unit);
    }

    /**
     * 触发
     */
    public void attach(){
        if(isCancel()) {
            return;
        }
        // 如果没有正在触发，则添加执行，否则，不执行，防止雪崩。
        if(attaching.compareAndSet(false, true)) {
            if (this.queue != null) {
                this.queue.add(this);
            } else {
                BaseTaskFactory.getFactory().addSlow(this);
            }
        }
    }

    @Override
    public int compareTo(Delayed o) {
        return (int)(this.getDelay(TimeUnit.SECONDS) - o.getDelay(TimeUnit.SECONDS));
    }

    /**
     * 取消执行
     */
    public void cancel() {
        this.loopCount = 0;
    }

    public boolean isCancel() {
        return this.loopCount == 0;
    }

    /**
     * 执行下一次
     * @return true:可以进行下一次，false，不能进行下一次
     */
    public boolean next() {
        this.expireTime = System.currentTimeMillis() + this.delayTime;
        if(loopCount < 0){
            return true;
        }
        if(loopCount == 0) {
            return false;
        }
        loopCount --;
        if(this.loopCount > 0){
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        super.run();
        // 执行完，修改attaching状态为false.
        attaching.set(false);
    }
}
