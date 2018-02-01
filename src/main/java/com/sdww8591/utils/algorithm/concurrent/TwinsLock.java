package com.sdww8591.utils.algorithm.concurrent;

import lombok.SneakyThrows;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class TwinsLock {

    private Sync sync = new Sync();

    private class Sync extends AbstractQueuedSynchronizer {

        private Sync() {
            setState(2);
        }

        //用于保证调用release()方法的线程曾经调用过lock()
        ThreadLocal<Integer> threadLocalHolder = new ThreadLocal<Integer>() {
            protected Integer initialValue() {
                return 0;
            }
        };

        @Override
        protected int tryAcquireShared(int arg) {
            for (;;) {
                int currentStatus = getState();
                int targetStatus = currentStatus - arg;
                if(targetStatus < 0
                        || compareAndSetState(currentStatus, targetStatus)) {
                    threadLocalHolder.set(threadLocalHolder.get() + arg);
                    return targetStatus;
                }
            }
        }

        @Override
        protected boolean tryReleaseShared(int arg) {
           if(threadLocalHolder.get() - arg < 0) {
               throw new IllegalMonitorStateException();
           }
           for(;;) {
               int current = getState();
               int targetStatus = current + arg;
               if(compareAndSetState(current, targetStatus)) {
                   threadLocalHolder.set(threadLocalHolder.get() - arg);
                   return true;
               }
           }
        }
    }

    public void lock() {
        sync.acquireShared(1);
    }

    public void unlock() {
        sync.releaseShared(1);
    }

    @SneakyThrows
    public static void main(String[] args) {
        final TwinsLock lock = new TwinsLock();
        class Worker extends Thread {

            @SneakyThrows
            @Override
            public void run() {
                while(true) {
                    lock.lock();
                    try {
                        TimeUnit.SECONDS.sleep(1);
                        System.out.println(Thread.currentThread().getName());
                        TimeUnit.SECONDS.sleep(1);
                    } finally {
                        lock.unlock();
                    }
                    TimeUnit.SECONDS.sleep(1);
                }
            }
        }

        for(int i = 0; i < 10; i++) {
            new Worker().start();
        }
    }

}
