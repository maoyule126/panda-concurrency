package myl.panda.dispose;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 销毁的抽象类
 * create by maoyule on 2019/1/7
 */
public abstract class Disposer implements IDispose {
    private static final Logger logger = LoggerFactory.getLogger(Disposer.class);

    protected boolean isDispose;

    public Disposer() {

    }

    @Override
    public void dispose() {
        if (isDispose) {
            return;
        }
        isDispose = true;
        try {
            doDispose();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public boolean isDispose() {
        return this.isDispose;
    }

    /**
     * 执行销毁操作
     */
    protected abstract void doDispose();
}
