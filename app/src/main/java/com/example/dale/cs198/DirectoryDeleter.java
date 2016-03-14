package com.example.dale.cs198;

import java.io.File;

/**
 * Created by jedpatrickdatu on 3/8/2016.
 */
public class DirectoryDeleter {
    public static void deleteDir(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles())
                deleteDir(child);
        }

        fileOrDirectory.delete();

    }
}
