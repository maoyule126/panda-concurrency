package myl.panda.dispose;

/**
 * 销毁的接口，建议所有的类都实现此接口
 * create by maoyule on 2019/1/7
 */
public interface IDispose {
    /**
     * 销毁自身
     * 实现此接口的类，可以在这个方法中将属性置空或销毁。
     * 这样做，可以在一定程度上加速垃圾回收器对对象的回收
     */
    void dispose();

    /**
     * 是否被销毁
     *
     * @return
     */
    boolean isDispose();
}
