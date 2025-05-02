package com.MyIAE;

public class Student {
    private String studentID;
    private String output;
    private boolean hasPassed;
    private boolean isCompiled;
    private boolean isRan;

    public Student(String studentID , boolean hasPassed) {
        this.studentID = studentID;
        this.hasPassed = hasPassed;
    }

    public Student(String studentID , boolean hasPassed , String output) {
        this.studentID = studentID;
        this.hasPassed = hasPassed;
        this.output = output;
    }

    public Student() {
        // Default constructor
    }

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public boolean getHasPassed() {
        return hasPassed;
    }

    public void setHasPassed(boolean hasPassed) {
        this.hasPassed = hasPassed;
    }

    public boolean isCompiled() {
        return isCompiled;
    }

    public void setCompiled(boolean isCompiled) {
        this.isCompiled = isCompiled;
    }

    public boolean isRan() {
        return isRan;
    }

    public void setIsRan(boolean isRan) {
        this.isRan =isRan;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

}