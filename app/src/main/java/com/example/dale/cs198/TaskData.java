package com.example.dale.cs198;

import android.util.Log;

import java.util.LinkedList;

import static org.bytedeco.javacpp.opencv_core.Mat;

/**
 * Created by jedpatrickdatu on 2/10/2016.
 */
public class TaskData {
    private static final String TAG = "testMessage";

    ThreadSafeQueue<Mat> detectQueue = new ThreadSafeQueue<Mat>();
    ThreadSafeQueue<Mat> recogQueue = new ThreadSafeQueue<Mat>();
    private boolean isMainThreadRunning = true;

    public synchronized void setThreadsToDie(){
        isMainThreadRunning = false;
        detectQueue.setNoMoreMatsComing();
        recogQueue.setNoMoreMatsComing();
    }

    public void faceDetectThreadAnnounceDeath() {
        synchronized(detectQueue) {
            detectQueue.notifyAll();
        }
    }

    public void facerecogThreadAnnounceDeath() {
        synchronized(recogQueue) {
            recogQueue.notifyAll();
        }
    }

    public void mainThreadWaitsForDetectThreadToDie() {
        synchronized(detectQueue) {
            try {
                Log.i(TAG, "Waiting for face detect thread to die...");
                detectQueue.wait();
                Log.i(TAG, "Face detect thread is dead!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void mainThreadWaitsForRecogThreadToDie() {
        synchronized(recogQueue) {
            try {
                Log.i(TAG, "Waiting for face recog thread to die...");
                recogQueue.wait();
                Log.i(TAG, "Face recog thread is dead!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized boolean isUIOpen(){
        return isMainThreadRunning;
    }



}

class ThreadSafeQueue<E>{

    private static final String TAG = "testMessage";
    private boolean isNoMoreMatsComing = false;

    private LinkedList<E> mq = new LinkedList<E>();

    public void add(E item) {
        synchronized(mq) {
            mq.addLast(item);
            mq.notifyAll();
        }
        //notifyAll();
    }

    public E poll() {
        Log.i(TAG, "now in poll");

        synchronized(mq) {
            Log.i(TAG, "now in poll sync block");
            if(isQueueEmpty()){
                if(isNoMoreMatsComing){
                    Log.i(TAG, "No more mats coming. Returning null.");
                    return null;//This null will tell the polling thread, which is either FaceDetectTask or FaceRecogTask, that it's time to die.
                }
                try {
                    Log.i(TAG, "Waiting...");
                    mq.wait();
                    Log.i(TAG, "Woke up!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return mq.poll();
        }

    }

    public boolean isQueueEmpty() {
        synchronized(mq) {
            return mq.isEmpty();
        }
    }

    public int size() {
        synchronized(mq) {
            return mq.size();
        }
    }

    public void setNoMoreMatsComing() {
        isNoMoreMatsComing = true;
        synchronized(mq) {
            mq.notifyAll();
        }
    }

}

