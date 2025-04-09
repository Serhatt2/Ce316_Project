package com.MyEde;

public class Student {
    private String studentID;
    private String result;
    private boolean hasPassed;
    private boolean isCompiled;
    private boolean isRan;

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public boolean hasPassed() {
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

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

}