package com.sdww8591;

import lombok.SneakyThrows;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Hello world!
 *
 */
public class App {

    private static ReentrantLock lock = new ReentrantLock();

    @SneakyThrows
    public static void main( String[] args ) {
        ReentrantLock lock1 = new ReentrantLock();
        lock1.lock();
        lock1.lock();
        lock1.unlock();

        List<Thread> threadList = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            Thread thread = new Thread(new Runnable() {

                @Override
                @SneakyThrows
                public void run() {
                    lock.lock();
                    System.out.println(Thread.currentThread().getName() + "locked!");
                    TimeUnit.SECONDS.sleep(30);
                    lock.unlock();
                    System.out.println(Thread.currentThread().getName() + "released!");
                }
            });
            threadList.add(thread);
        }

        for(Thread thread : threadList) {
            thread.start();
            TimeUnit.SECONDS.sleep(1);
        }

        TimeUnit.SECONDS.sleep(5);
        threadList.get(1).interrupt();

        TimeUnit.SECONDS.sleep(9999);
    }
}
