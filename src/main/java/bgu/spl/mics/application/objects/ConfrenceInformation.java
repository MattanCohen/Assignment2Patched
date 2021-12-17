package bgu.spl.mics.application.objects;

import java.util.LinkedList;

/**
 * Passive object representing information on a conference.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class ConfrenceInformation {

    private String name;
    private int date;
    LinkedList<Model> modelsToPublish;

    public ConfrenceInformation(String _name, int _date){
        name=_name;
        date=_date;
        modelsToPublish=new LinkedList<Model>();
    }

    public void setModelsToPublish(LinkedList<Model> toSet){
        modelsToPublish=toSet;
    }

    public LinkedList<Model> getModelsToPublish() {
        return modelsToPublish;
    }

    public String getName() {
        return name;
    }

    public int getDate() {
        return date;
    }
}
