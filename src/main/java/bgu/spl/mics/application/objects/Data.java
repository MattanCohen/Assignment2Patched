package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Data {
    /**
     * Enum representing the Data type.
     */
    public enum Type {
        Images, Text, Tabular
    }

    private Type type;
    private int processed;
    private int size;

    public Data(Type _type, int _size){
        type=_type;
        size=_size;
        processed=0;
    }

    public Type getType() {
        return type;
    }

    public int getProcessed() {
        return processed;
    }

    public int getSize() {
        return size;
    }

    //return true if data was processed
    public boolean isProcessed(){
        return processed==1;
    }
}
