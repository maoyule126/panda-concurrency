package myl.panda.concurrency.clouds;

import java.util.concurrent.TimeUnit;

/**
 * create by maoyule on 2019/2/23
 */
public interface ICloudLock {
    void lock();
    void lockInterruptibly() throws InterruptedException;
    boolean tryLock();
    boolean tryLock(long time, TimeUnit unit) throws InterruptedException;
    void unlock();
    boolean isLock();
}
