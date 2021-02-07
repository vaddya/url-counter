package com.vaddya.urlcounter.local;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jetbrains.annotations.NotNull;

/**
 * In memory URL counter implementation based on two-level collection.
 * 
 * Operations add(String) & top(int) are O(1), though require some interaction with memory (due to references),
 * and lock protection (due to usage of non trade-safe collections).
 *
 * top -> 5 -> google.com <-> mail.ru <-> twitter.com
 *        |
 *        2 -> yandex.ru <-> yahoo.com
 *        |
 * bot -> 1 -> example.com
 *
 * Vertical doubly-linked list - counter nodes (each node points to the first domain node)
 * Horizontal doubly-linked list - domain nodes (each node points back to the counter node)
 * 
 * In the example, if we need to get top(4) we will traverse all nodes with the counter=5 first,
 * and then we will go down one level to the counter=2 to get the forth domain.
 * New domain nodes are created in the bottom list (with counter 1).
 */
public class InMemoryUrlCounter implements UrlCounter {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();
    private final Map<String, DomainNode> domains;
    private CounterNode top;
    private final CounterNode bot;

    public InMemoryUrlCounter() {
        this.domains = new HashMap<>();
        this.bot = new CounterNode(1);
        this.top = bot;
    }

    @Override
    public void add(@NotNull final String domain) {
        writeLock.lock();
        try {
            if (domains.containsKey(domain)) {
                DomainNode node = domains.get(domain);
                increment(node);
            } else {
                DomainNode node = new DomainNode(domain);
                domains.put(domain, node);
                node.next = bot.domains;
                node.counter = bot;
                if (node.next != null) {
                    node.next.prev = node;
                }
                bot.domains = node;
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    @NotNull
    public List<String> top(final int n) {
        readLock.lock();
        try {
            List<String> result = new ArrayList<>();
            DomainNode curr = top.domains;
            while (result.size() < n && curr != null) {
                result.add(curr.domain);
                if (curr.next != null) {
                    curr = curr.next;
                } else if (curr.counter.prev != null) {
                    curr = curr.counter.prev.domains;
                } else {
                    break;
                }
            }
            return result;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Map<String, Integer> topCount(int n) {
        readLock.lock();
        try {
            Map<String, Integer> result = new LinkedHashMap<>();
            DomainNode curr = top.domains;
            while (result.size() < n && curr != null) {
                result.put(curr.domain, curr.counter.count);
                if (curr.next != null) {
                    curr = curr.next;
                } else if (curr.counter.prev != null) {
                    curr = curr.counter.prev.domains;
                } else {
                    break;
                }
            }
            return result;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Should be called only under write lock!
     */
    private void increment(@NotNull final DomainNode node) {
        CounterNode counter = node.counter;
        // remove node from counter domain list
        if (node.next != null) {
            node.next.prev = node.prev;
        }
        if (node.prev != null) {
            node.prev.next = node.next;
        }
        if (counter.domains == node) {
            counter.domains = node.next;
            if (node.next == null && counter != bot) {
                // remove counter node
                if (counter.prev != null) {
                    counter.prev.next = counter.next;
                }
                if (counter.next != null) {
                    counter.next.prev = counter.prev;
                }
            }
        }
        node.prev = null;
        node.next = null;
        if (counter.next == null) {
            // insert new top counter
            counter.next = new CounterNode(counter.count + 1);
            node.counter = counter.next;
            if (counter.domains == null && counter != bot) {
                // removed counter
                counter.next.prev = counter.prev;
                counter.prev.next = counter.next;
            } else {
                counter.next.prev = counter;
                if (counter.prev != null) counter.prev.next = counter;
            }
            counter.next.domains = node;
            top = counter.next;
        } else {
            if (counter.next.count == counter.count + 1) {
                // count matched
                node.next = counter.next.domains;
                if (node.next != null) {
                    node.next.prev = node;
                }
                node.counter = counter.next;
                counter.next.domains = node;
            } else {
                // insert count node
                CounterNode newCounter = new CounterNode(counter.count + 1);
                node.counter = newCounter;
                newCounter.next = counter.next;
                newCounter.prev = counter;
                counter.next.prev = newCounter;
                counter.next = newCounter;
                newCounter.domains = node;
            }
        }
    }

    private static class CounterNode {
        final int count;
        CounterNode next;
        CounterNode prev;
        DomainNode domains;

        CounterNode(final int count) {
            this.count = count;
        }
    }

    private static class DomainNode {
        final String domain;
        DomainNode next;
        DomainNode prev;
        CounterNode counter;

        DomainNode(@NotNull final String domain) {
            this.domain = domain;
        }
    }
}
