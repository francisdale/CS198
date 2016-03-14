package com.example.dale.cs198;

import java.util.Date;

/**
 * Created by DALE on 3/13/2016.
 */
public class ReportItem {

    private String reportPath;
    private Date date;
    private String className;
    private String fileName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ReportItem(String className, Date date, String reportPath) {
        this.className = className;
        this.date = date;
        this.reportPath = reportPath;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getReportPath() {
        return reportPath;
    }

    public void setReportPath(String reportPath) {
        this.reportPath = reportPath;
    }
}
