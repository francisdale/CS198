package com.example.dale.cs198;

import static org.bytedeco.javacpp.opencv_core.Mat;

/**
 * Created by jedpatrickdatu on 2/10/2016.
 */
public class TaskData {

    public static final String detectOutputDir = "cs198/detectedFaces";
    GenQueue<Mat> detectQueue = new GenQueue<Mat>();
    GenQueue<Mat> recogQueue = new GenQueue<Mat>();
    private boolean isMainThreadOpened = true;



    public synchronized void setIsUIOpenedToFalse(){
        isMainThreadOpened = false;
    }

    public synchronized boolean isUIOpened(){
        return isMainThreadOpened;
    }






}
