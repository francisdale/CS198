package com.example.dale.cs198;

/**
 * Created by DALE on 1/26/2016.
 */
public class StudentItem {

    private String studentNumber;
    private  String lastName;
    private  String firstName;
    private int abscences;

    public StudentItem(String studentNumber,String lastName,String firstName){
        this.setStudentNumber(studentNumber);
        this.setLastName(lastName);
        this.setFirstName(firstName);
        this.setAbscences(0);
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public int getAbscences() {
        return abscences;
    }

    public void setAbscences(int abscences) {
        this.abscences = abscences;
    }
}
