package bgu.spl.mics.application.objects;

import java.util.LinkedList;

/**
 * Passive object representing single student.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Student {
    /**
     * Enum representing the Degree the student is studying for.
     */
    public enum Degree {
        MSc, PhD
    }

    private String name;
    private String department;
    private Degree status;
    //number of published results. should increase when conference publishes result
    private int publications;
    private int papersRead;

    public Student(){}

    public Student(String _name, String _department, Degree _status){
        name=_name;
        department=_department;
        status=_status;
        publications=0;
        papersRead=0;
    }

    public String getName() {
        return name;
    }

    public Degree getStatus() {
        return status;
    }

    // increase publications/papers read by 1
    public void publicationsIncrement() { publications++;}
    public void papersReadIncrement() { papersRead++;}

    public int getPublications() {
        return publications;
    }

    public int getPapersRead() {
        return papersRead;
    }

    public String getDepartment() {
        return department;
    }
}
