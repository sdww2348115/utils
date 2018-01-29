package com.sdww8591.utils.algorithm.concurrent;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

public class BlockingCLHLock {

    /**
     * 用于标记当前lock的状态。
     * 由于BlockingCLHLock是由释放锁的线程来唤醒等待线程，如果lock此时没有被任何线程锁拥有，新添加的线程将由于没有唤醒者无限等待
     * 因此对于没有被任何线程所拥有的锁的处理方式将被区分开来区别对待
     */
    private AtomicBoolean lockStatus = new AtomicBoolean(false);

    private AtomicReference<Node> tail = new AtomicReference<>();

    private AtomicReference<Node> head = new AtomicReference<>();

    private ThreadLocal<Node> current = new ThreadLocal<Node>(){
        @Override
        protected Node initialValue() {
            return new Node(false);
        }
    };

    public void lock() {
        if(lockStatus.compareAndSet(false, true)) {
            Node currentNode = current.get();
            currentNode.setStatus(false);
            head.set(currentNode);
            tail.set(currentNode);
        } else {
            while(tail.get() == null) {
                //wait the first thread complete initialization
            }
            Node currentNode = current.get();
            currentNode.setStatus(false);
            Node tailNode = tail.getAndSet(currentNode);
            currentNode.previous = tailNode;
            tailNode.next = currentNode;
            LockSupport.park(currentNode);
        }
    }

    public void release() {
        Node currentNode = current.get();
        currentNode.setStatus(true);

    }

    class Node {
        /**
         * status为true代表线程已释放lock
         * status为false代表线程未拿到或者已经拿到lock但未释放
         */
        @Setter
        @Getter
        private volatile boolean status;

        @Getter @Setter
        private Node previous;

        @Getter @Setter
        private Node next;

        public Node(boolean status) {
            this.status = status;
        }
    }
}
