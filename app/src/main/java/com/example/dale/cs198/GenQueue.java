package com.example.dale.cs198;

import java.util.LinkedList;

/**
 * Created by jedpatrickdatu on 2/10/2016.
 * Reference: http://www.tutorialspoint.com/javaexamples/data_queue.htm
 */
public class GenQueue<E>{

    private LinkedList<E> mq = new LinkedList<E>();

    public synchronized void add(E item) {
        mq.addLast(item);
    }

    public synchronized E poll() {
        return mq.poll();
    }

    public synchronized boolean hasItems() {
        return !mq.isEmpty();
    }

    public synchronized int size() {
        return mq.size();
    }

}
