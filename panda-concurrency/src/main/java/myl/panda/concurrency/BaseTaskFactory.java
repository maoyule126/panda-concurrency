package myl.panda.concurrency;

import myl.panda.concurrency.pools.TaskPool;
import myl.panda.concurrency.tasks.ITask;
import myl.panda.dispose.Disposer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

/**
 * create by maoyule on 2019/1/7
 * 继承类需用@Configuration注解标记
 */
public class BaseTaskFactory extends Disposer{
    private static BaseTaskFactory factory = null;

    public static BaseTaskFactory getFactory(){
        return factory;
    }

    private int mainThreadCount;
    private int slowThreadCount;
    /**
     * 主业务任务池
     */
    private TaskPool mainPool;
    /**
     * 慢业务任务池
     */
    private TaskPool slowPool;

    public BaseTaskFactory(){
        init();
    }

    public BaseTaskFactory(int mainThreadCount, int slowThreadCount){
        this.mainThreadCount = mainThreadCount;
        this.slowThreadCount = slowThreadCount;
        init();
    }

    @Override
    protected void doDispose() {
        if(mainPool != null){
            mainPool.dispose();
        }
        if(slowPool != null){
            slowPool.dispose();
        }
    }

    public void init() {
        if(factory == null) {
            factory = this;
        }else{
            throw new RuntimeException("factory has init.");
        }
        int procCount = Runtime.getRuntime().availableProcessors();
        /* 如果没有设置，或者设置参数非法
         * 主线程池线程数为cpu核数*2， 慢业务线程池数为cpu核数*4
         */
        mainPool = new TaskPool().init(mainThreadCount <= 0 ? procCount * 2 : mainThreadCount, "task");
        slowPool = new TaskPool().init(slowThreadCount <= 0 ? procCount * 4 : slowThreadCount, "slow");
    }

    public void add(ITask task){
        mainPool.add(task);
    }

    public void submit(Runnable runnable){
        mainPool.submit(runnable);
    }

    public void addSlow(ITask task){
        slowPool.add(task);
    }

    public void submitSlow(Runnable runnable){
        slowPool.submit(runnable);
    }

    public TaskPool getMainPool() {
        return mainPool;
    }

    public TaskPool getSlowPool() {
        return slowPool;
    }
}
