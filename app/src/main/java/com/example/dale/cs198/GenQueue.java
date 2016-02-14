package com.example.dale.cs198;

import android.util.Log;

import java.util.LinkedList;

/**
 * Created by jedpatrickdatu on 2/10/2016.
 * Reference: http://www.tutorialspoint.com/javaexamples/data_queue.htm
 */
public class GenQueue<E>{

    private static final String TAG = "testMessage";

    private LinkedList<E> mq = new LinkedList<E>();

    public void add(E item) {
        Log.i(TAG, "now in add");
        synchronized(mq) {
            Log.i(TAG, "now in add sync block");
            mq.addLast(item);
            mq.notifyAll();
        }
        Log.i(TAG, "add done");
        //notifyAll();
    }

    public E poll() {
        Log.i(TAG, "now in poll");

        synchronized(mq) {
            Log.i(TAG, "now in poll sync block");
            if(isQueueEmpty()){
                try {
                    Log.i(TAG, "Waiting for a train...");
                    mq.wait();
                    Log.i(TAG, "We will be together.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Log.i(TAG, "poll done");
            return mq.poll();
        }

    }

    public boolean isQueueEmpty() {
        synchronized(mq) {
            return mq.isEmpty();
        }
    }

    public int size() {
        synchronized (mq) {
            return mq.size();
        }
    }

}
