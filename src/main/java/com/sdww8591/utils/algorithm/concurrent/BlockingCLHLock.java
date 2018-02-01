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
            Node result = new Node(false);
            result.setThread(Thread.currentThread());
            return result;
        }
    };

    public void lock() {
        if(!lockStatus.compareAndSet(false, true)) {

            Node currentNode = current.get();
            //将currentNode节点放入Queue中
            for(;;) {
                if(tail.get() == null) {
                    if(tail.compareAndSet(null, new Node(false))) {
                        head.set(tail.get());
                    }
                } else {
                    Node tailNode = tail.get();
                    currentNode.setPrevious(tailNode);
                    if(tail.compareAndSet(tailNode, currentNode)) {
                        tailNode.setNext(currentNode);
                        break;
                    }
                }
            }

            //实现阻塞
            for(;;) {
                //在我们将node放至队列末尾时，前面的节点释放了锁，当前线程可以直接获取到锁资源，不用block
                if(currentNode.getPrevious().equals(head.get())
                        && lockStatus.compareAndSet(false, true)) {
                    //由于将node添加至queue了，这里需要将node从等待队列中取出
                    head.set(currentNode);
                    currentNode.setNext(null); //这里是为了帮助GC
                    break;
                } else {
                    LockSupport.park(currentNode);
                    break;
                }
            }
        }
    }

    public void release() {
        for(;;) {
            Node headNode = head.get();
            if(headNode.getNext() == null) {
                this.lockStatus.compareAndSet(true, false);
            }
            Node nextHead = headNode.getNext();
            if(head.compareAndSet(headNode, nextHead)) {
                nextHead.setPrevious(null);
                LockSupport.unpark(nextHead.thread);
            }
        }
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
        private volatile Node previous;

        @Getter @Setter
        private volatile Node next;

        @Getter @Setter
        private volatile Thread thread;

        public Node(boolean status) {
            this.status = status;
        }
    }
}
