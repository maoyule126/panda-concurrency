package myl.panda.utils;

import myl.panda.dispose.IDispose;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 销毁功能类
 * create by maoyule on 2019/1/7
 */
public class DisposeUtils {
    /**
     * 销毁对象，对象需要实现IDispose接口
     *
     * @param o
     */
    public static void dispose(IDispose o) {
        if (o != null) {
            o.dispose();
        }
    }

    /**
     * 销毁Map
     *
     * @param map
     */
    public static <K, V extends IDispose> void disposeMap(Map<K, V> map) {
        if (map == null) {
            return;
        }
        for (V v : map.values()) {
            v.dispose();
        }
        map.clear();
    }

    /**
     * 销毁list
     *
     * @param list
     */
    public static <T extends IDispose> void disposeList(List<T> list) {
        Iterator<T> iter = list.iterator();
        while (iter.hasNext()) {
            iter.next().dispose();
            iter.remove();
        }
    }

    /**
     * 销毁数组
     *
     * @param arr
     */
    public static <T extends IDispose> void disposeArray(T[] arr) {
        int i;
        int len = arr.length;
        for (i = 0; i < len; i++) {
            T t = arr[i];
            if (t != null) {
                t.dispose();
            }
            arr[i] = null;
        }
    }
}
