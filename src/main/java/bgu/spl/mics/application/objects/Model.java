package bgu.spl.mics.application.objects;

import com.google.gson.JsonElement;

/**
 * Passive object representing a Deep Learning model.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Model {

    public enum Status{
        PreTrained, Training,Trained,Tested
    }
    public enum Result{
        None,Good,Bad
    }

    // model name is unique
    final private String name;
    //the data the model should train on
    private Data data;
    //the student which created the model
    final private Student student;
    private Status status;
    private Result result;

    public Model(String _name, Data _data, Student _student){
        name=_name;
        data=_data;
        student=_student;
        status=Status.PreTrained;
        result=Result.None;
    }

    public Data getData() {return data;}

    public Status getStatus() {
        return status;
    }

    public Result getResult() {
        return result;
    }

    public String getName() {
        return name;
    }

    public Student getStudent() {
        return student;
    }

    public void setStatus(Status other){
        status=other;
    }

    public void setResult(Result other){
        result=other;
    }

}
