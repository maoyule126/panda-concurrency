package myl.panda.concurrency.pools;

import myl.panda.concurrency.queues.ITaskQueue;
import myl.panda.concurrency.tasks.ITask;
import myl.panda.dispose.Disposer;

import java.util.concurrent.*;

/**
 * 任务池，对jdk线程池的包装
 * create by maoyule on 2019/1/7
 */
public class TaskPool extends Disposer {
    private ExecutorService taskService;

    public TaskPool(){

    }

    @Override
    protected void doDispose() {
        taskService.shutdown();
    }

    /**
     * 初始化
     * @param taskService
     */
    public TaskPool init(ExecutorService taskService) {
        this.taskService = taskService;
        return this;
    }

    /**
     * 初始化
     * @param threadCount 线程池中线程数量
     * @param name 线程池名称
     */
    public TaskPool init(int threadCount, String name) {
        this.taskService = newFixedThreadPool(threadCount, name);
        return this;
    }

    /**
     * 初始化单个单个线程池
     * @param name
     * @return
     */
    public TaskPool initSingle(String name) {
        this.taskService = Executors.newSingleThreadExecutor(new MyThreadFactory(name));
        return this;
    }


    /**
     * 创建线程数固定的线程池
     * @param threadCount 线程数量
     * @param sThreadName
     * @return
     */
    private static MyThreadPoolExecutor newFixedThreadPool(int threadCount,
                                                      String sThreadName) {
        return new MyThreadPoolExecutor(threadCount, threadCount, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
                new MyThreadFactory(sThreadName), new ThreadPoolExecutor.AbortPolicy());
    }

    public void add(ITask task) {
        taskService.submit(task);
    }

    public void add(ITaskQueue queue) {
        taskService.submit(queue);
    }

    /**
     * 增加任务
     * @param runnable
     */
    public void submit(Runnable runnable) {
        taskService.submit(runnable);
    }

    /**
     * 停止某个线程，强烈建议仅在发现死锁时调用
     * @param threadName
     */
    public void stopThread(String threadName){
        if(taskService instanceof MyThreadPoolExecutor){
            ((MyThreadPoolExecutor)taskService).stopThread(threadName);
        }
    }
}
