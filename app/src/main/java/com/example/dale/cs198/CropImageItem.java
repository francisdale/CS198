package com.example.dale.cs198;

/**
 * Created by DALE on 2/6/2016.
 */
public class CropImageItem {

    private String path;
    private String fileName;
    private int pos;

    public CropImageItem(String path, String fileName) {
        this.setPath(path);
        this.setFileName(fileName);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }
}
