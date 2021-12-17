package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */

public class DataBatch {

    //the data the batch belongs to
    private Data data;
    //index of first sample in batch
    private int start_index;
    //true if data batch is processed
    private boolean processed;

    public DataBatch (Data _data, int _start_index){
        data=_data;
        start_index=_start_index;
        processed=false;
    }

    public int getTimeToDoBatch(CPU cpu){
        int tickMultiplier = 1;
        // based on data type we ticks needed to process data change
        if (data.getType()==Data.Type.Images) {tickMultiplier=4;}
        else if (data.getType()==Data.Type.Tabular) {tickMultiplier=2;}
        int cores = cpu.getCores();
        return (32/cores)*tickMultiplier;
    }

    public boolean isProcessed() {return processed;}
    public void finishProcessing(){processed=true;}
    public Data getData() {
        return data;
    }

    public int getStart_index() {
        return start_index;
    }
}
