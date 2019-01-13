package myl.panda.timers;

import myl.panda.concurrency.pools.MyThreadFactory;
import myl.panda.dispose.Disposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Set;
import java.util.concurrent.*;

/**
 * create by maoyule on 2019/1/9
 */
public class TimerService extends Disposer implements BeanPostProcessor {
    private static final Logger logger = LoggerFactory.getLogger(TimerService.class);

    private static TimerService service = null;

    public static TimerService getService(){
        return service;
    }

    private ScheduledExecutorService scheduledService;

    private ExecutorService delayService;

    private ScheduledFuture secondFuture;

    private Set<SecondTask> secondTickerSet;

    private DelayQueue<LoopTask> delayQueue;

    private DelayedRunnable delayedRunnable;

    public TimerService() {
        if(service == null) {
            service = this;
        }else{
            throw new RuntimeException("timer service has init");
        }
        init();
    }

    @Override
    protected void doDispose() {
        if(delayedRunnable != null){
            delayedRunnable.dispose();
        }
        if(delayService != null) {
            delayService.shutdown();
        }
        if(scheduledService != null){
            scheduledService.shutdown();
        }
    }

    private void init() {
        delayQueue = new DelayQueue<>();
        secondTickerSet = ConcurrentHashMap.newKeySet();

        scheduledService = Executors.newScheduledThreadPool(1, new MyThreadFactory(
                "timer"));
        secondFuture = scheduledService.scheduleWithFixedDelay(new SecondRunner(), 0, 1,
                TimeUnit.SECONDS);
        delayService = Executors.newSingleThreadExecutor(new MyThreadFactory("delayed"));
        delayedRunnable = new DelayedRunnable();
        delayService.submit(delayedRunnable);
    }

    public ScheduledExecutorService getScheduledService() {
        return this.scheduledService;
    }

    /**
     * 添加每秒调用的task
     *
     * @param ticker
     */
    public void addTicker(SecondTask ticker) {
        secondTickerSet.add(ticker);
    }

    public void removeTicker(SecondTask ticker) {
        secondTickerSet.remove(ticker);
    }

    public void addDelay(LoopTask loopTask) {
        this.delayQueue.offer(loopTask);
    }

    private class DelayedRunnable extends Disposer implements Runnable {
        private boolean isRunning = true;
        private Thread currentThread;

        public DelayedRunnable(){
        }

        @Override
        protected void doDispose() {
            isRunning = false;
            if(currentThread != null){
                currentThread.interrupt();
            }
        }

        @Override
        public void run() {
            currentThread = Thread.currentThread();
            while (isRunning) {
                try {
                    LoopTask delay = delayQueue.take();
                    if (delay == null) {
                        Thread.sleep(100);
                        continue;
                    }
                    try {
                        delay.attach();
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    if (delay.next()) {
                        delayQueue.offer(delay);
                    }
                }catch (Exception e){
                    if(!(e instanceof InterruptedException)){
                        logger.error(e.getMessage(), e);
                    }
                    try {
                        Thread.sleep(100);
                    }catch (Exception ex){
                        if(!(e instanceof InterruptedException)){
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
        }
    }

    private class SecondRunner implements Runnable {
        @Override
        public void run() {
            try {
                // second tick
                for (SecondTask ticker : secondTickerSet) {
                    ticker.attach();
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
