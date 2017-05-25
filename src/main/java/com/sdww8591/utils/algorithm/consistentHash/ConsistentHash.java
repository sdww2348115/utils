package com.sdww8591.utils.algorithm.consistentHash;

import com.sdww8591.utils.third.MurmurHash;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by wangxuan on 2017/5/25.
 */
public class ConsistentHash <T> {

    private final int virtualNodeCount;

    private static final int DEFAULT_VIRTUAL_NODE = 128;

    private static final Random rand = new Random(System.currentTimeMillis());

    private TreeMap<Long, T> map = new TreeMap<>();

    public ConsistentHash(int virtualNodeCount, Collection<T> objects) {

        if(objects == null || objects.isEmpty()) {

            throw new IllegalArgumentException("target collection must be not empty!");
        }
        this.virtualNodeCount = virtualNodeCount;
        init(objects);
    }

    public ConsistentHash(Collection<T> objects) {

        this(DEFAULT_VIRTUAL_NODE, objects);
    }

    private void init(Collection<T> objects) {

        for(T t: objects) {

            for(int i = 0; i < virtualNodeCount; i++) {

                map.put(rand.nextLong(), t);
            }
        }
    }

    public T getShard(String key) {

        Long hashKey = MurmurHash.hash(key);
        Map.Entry<Long, T> target = map.higherEntry(hashKey);
        return target == null? map.firstEntry().getValue() : target.getValue();
    }

    public static void main(String[] args) {

        List<String> targets = Arrays.asList("1","2","3","4","5");
        ConsistentHash<String> hashEntity = new ConsistentHash(targets);

        Map<String, AtomicInteger> countMap = new HashMap<>();
        for(String target: targets) {

            countMap.put(target, new AtomicInteger(0));
        }
        for(int i = 0; i < 100000; i++) {

            String key = String.valueOf(rand.nextLong());
            String value = hashEntity.getShard(key);
            countMap.get(value).getAndIncrement();
            System.out.println(String.format("current %s, target: %s", key, hashEntity.getShard(key)));
        }

        for(Map.Entry<String, AtomicInteger> entry: countMap.entrySet()) {

            System.out.println(String.format("%s has been invoked %d times!", entry.getKey(), entry.getValue().get()));
        }
    }
}
