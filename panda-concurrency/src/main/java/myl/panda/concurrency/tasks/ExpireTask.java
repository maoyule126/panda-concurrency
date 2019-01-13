package myl.panda.concurrency.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * create by maoyule on 2019/1/8
 */
public abstract class ExpireTask extends AbstractTask {
    private static final long DEFAULT_EXPIRE_INTERVAL = 1000L;

    private static final Logger logger = LoggerFactory.getLogger(ExpireTask.class);

    private long expireTime;

    public ExpireTask(){
        this(DEFAULT_EXPIRE_INTERVAL);
    }

    public ExpireTask(long expireInterval){
        if(expireInterval <= 0){
            expireTime = DEFAULT_EXPIRE_INTERVAL;
        }
        this.expireTime = System.currentTimeMillis() + expireInterval;
    }

    @Override
    public void run() {
        try {
            if (isExpire()) {
                doExpire();
                return;
            }
            execute();
        }catch (Exception e){
            logger.error(e.getMessage(), e);
        }
    }

    public boolean isExpire(){
        return System.currentTimeMillis() - this.expireTime > 0;
    }

    protected void doExpire(){
        logger.error("{} is timeout when executed. ", this.toString());
    }
}
