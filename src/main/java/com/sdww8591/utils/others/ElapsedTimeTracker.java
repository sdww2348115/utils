package com.sdww8591.utils.others;

import java.util.HashMap;
import java.util.Map;

/**
 * 一个简单的用于性能测试的小工具，利用ThreadLocal的性质来记录线程执行各种操作所用的时间
 */
public class ElapsedTimeTracker {

    private static final ThreadLocal<Map<Object, Long>> trackerMapHandler = new ThreadLocal<Map<Object, Long>>() {
        @Override
        protected Map<Object, Long> initialValue() {
            return new HashMap<Object, Long>();
        }
    };

    /**
     * 初始化key
     * @param key
     */
    public static void startRecord(Object key) {
        trackerMapHandler.get().put(key, System.currentTimeMillis());
    }

    /**
     * 获取该key从初始化到调用该方法所耗的时间
     * @param key
     * @return
     */
    public static Long getElapsedTime(Object key) {
        return trackerMapHandler.get().containsKey(key) ?
                System.currentTimeMillis() - trackerMapHandler.get().get(key) : null;
    }

    /**
     * 将key从map中移除
     * @param key
     */
    public static void clearRecord(Object key) {
        trackerMapHandler.get().remove(key);
    }

}
