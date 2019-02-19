package myl.panda.concurrency.pools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.concurrent.*;

/**
 * create by maoyule on 2019/2/19
 */
public class MyThreadPoolExecutor extends ThreadPoolExecutor {
    private static final Logger logger = LoggerFactory.getLogger(MyThreadPoolExecutor.class);

    public MyThreadPoolExecutor(int corePoolSize,
                                int maximumPoolSize,
                                long keepAliveTime,
                                TimeUnit unit,
                                BlockingQueue<Runnable> workQueue,
                                ThreadFactory threadFactory,
                                RejectedExecutionHandler handler){
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    /**
     * 停止某个线程，强烈建议仅在发现死锁时调用
     */
    public void stopThread(String threadName){
        try {
            Class<?> workerClazz = Class.forName("java.util.concurrent.ThreadPoolExecutor$Worker");
            Class[] cArgs = new Class[2];
            cArgs[0] = workerClazz;
            cArgs[1] = boolean.class;
            Field field = this.getClass().getDeclaredField("workers");
            field.setAccessible(true);
            Method processWorkerExitMethod = this.getClass().getDeclaredMethod("processWorkerExit", cArgs);
            processWorkerExitMethod.setAccessible(true);

            HashSet<Object> set = (HashSet<Object>) field.get(this);
            for(Object obj : set){
                Field threadField = obj.getClass().getDeclaredField("thread");
                threadField.setAccessible(true);
                Thread thread = (Thread)threadField.get(obj);
                if(thread.getName().equals(threadName)){
                    logger.info("thread stop. thread.name:{}, thread.state:{}", thread.getName(), thread.getState());
                    thread.stop();
                    processWorkerExitMethod.invoke(this, obj, true);
                    break;
                }
            }
        }catch (Exception e){
            logger.error(e.getMessage(), e);
        }
    }
}
