package myl.panda.concurrency.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * create by maoyule on 2019/1/8
 */
public abstract class MonitorTask extends AbstractTask {
    private static final Logger logger = LoggerFactory.getLogger(MonitorTask.class);
    private static final long DEFAULT_EXE_TIME_LIMIT = 100;

    private long exeTimeLimit;

    public MonitorTask(){
        this(DEFAULT_EXE_TIME_LIMIT);
    }


    public MonitorTask(long exeTimeLimit){
        super();
        this.exeTimeLimit = exeTimeLimit;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        super.run();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        if(duration > exeTimeLimit){
            exeLong(duration);
        }
    }

    protected void exeLong(long duration){
        logger.warn("{} executed too long.cost time:{}", this.toString(), duration);
    }

}
