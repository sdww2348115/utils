package com.sdww8591.utils.algorithm.concurrent;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * CLH(Craig, Landin, and Hagersten  locks): 是一个自旋锁，能确保无饥饿性，提供先来先服务的公平性，适用于SMP系统。
 * CLH锁基于链表实现，通过不断spin轮询前驱的status来判断自己是否获取到锁资源。
 * CLH锁空间复杂度为O(L+n),其中L为CLHLock实例个数，每个实例含有一个tail对象；n为线程数，每个线程含有一个Node对象。
 */
public class CLHLock {

    private AtomicReference<Node> tail = new AtomicReference<>(new Node(true));
    private ThreadLocal<Node> current = new ThreadLocal<Node>(){
        @Override
        protected Node initialValue() {
            return new Node(false);
        }
    };

    public void lock() {
        Node currentNode = current.get();
        currentNode.setStatus(false);
        Node tailNode = tail.getAndSet(currentNode);
        currentNode.setPrevious(tailNode);
        while (!tailNode.isStatus()) {
            //spin
        }
    }

    public void release() {
        Node currentNode = current.get();
        currentNode.setStatus(true);
        //此处本应该set一个new node，同时当前Node的previousNode生命周期也已经结束，此时应该被VM回收
        //基于减少Node对象创建的目的，可以将已经没有使用价值的previousNode当做一个全新的New Node使用，达到优化的目的。
        current.set(currentNode.previous);
    }

    class Node {
        /**
         * status为true代表线程已释放lock
         * status为false代表线程未拿到或者正拿到lock
         */
        @Setter @Getter
        private volatile boolean status;

        @Getter @Setter
        private Node previous;

        public Node(boolean status) {
            this.status = status;
        }
    }

    public static void main(String[] args) {
        final CLHLock clhLock = new CLHLock();
        for(int i = 0; i < 3; i++) {
            final int j = i;
            new Thread(new Runnable() {
                @Override
                @SneakyThrows
                public void run() {
                    System.out.println("thread-" + j +" started!");
                    clhLock.lock();
                    System.out.println("thread-" + j +" get lock!");
                    TimeUnit.SECONDS.sleep(2);
                    System.out.println("thread-" + j +" release lock!");
                    clhLock.release();
                }
            }).start();
        }
    }
}
