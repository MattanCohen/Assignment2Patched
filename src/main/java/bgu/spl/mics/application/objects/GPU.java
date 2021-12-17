package bgu.spl.mics.application.objects;

//import com.sun.org.apache.xpath.internal.operations.Mod;
//import sun.awt.image.ImageWatched;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU {
    /**
     * Enum representing the type of the GPU.
     */
    public enum Type {RTX3090, RTX2080, GTX1080}

    final private Type type;
    private int currentSpace;
    // tickCounter = number of ticks counted needed to train model
    private LinkedList<DataBatch> processingBatches;
    AtomicInteger tickCounter;
    LinkedList<Model> modelsWorked;

    /**
     * @param _type    GPU type
     * @param _cluster GPU is linked to
     * @post model is unassigned
     */
    public GPU(Type _type, Cluster _cluster) {
        type = _type;
        modelsWorked=new LinkedList<>();
        if (type == Type.RTX3090)
            currentSpace = (32);
        else if (type == Type.RTX2080)
            currentSpace = (16);
        else if (type == Type.GTX1080)
            currentSpace = (8);
        tickCounter = new AtomicInteger(0);
        processingBatches = new LinkedList<>();
    }


    /**
     * receive a model (change newModel's status to training)
     * use cpu to process the data and then
     * train it and change newModel's status to trained
     *
     * @pre model.getStatus() = Status.PreTrained
     * @inv model.getStatus() = Status.Training
     * @post model.getStatus() = Status.Trained
     */

    public int getCurrentSpace() {
        return currentSpace;
    }

    public LinkedList<DataBatch> dataToBatches(Data data) {
        LinkedList<DataBatch> ans = new LinkedList<>();
        for (int i = 0; i < (int) (data.getSize() / 1000); i++) {
            ans.add(new DataBatch(data, i));
        }
        return ans;
    }

    public void removeProcessedBatch(DataBatch processedData) {
        if (processingBatches.contains(processedData)) {
            processingBatches.remove(processedData);
            currentSpace++;
        }
    }

    public LinkedList<String> getNamesModelsTrained() {
        LinkedList<String> names=new LinkedList<>();
        for (Model m:modelsWorked)
            names.add(m.getName());
        return names;
    }

    public void addModel(Model model){
        modelsWorked.add(model);
    }
    public Boolean addBatch(DataBatch unprocessedData) {
        if (unprocessedData != null && currentSpace > 0) {
            currentSpace--;
            processingBatches.add(unprocessedData);
            return true;
        }
        return false;
    }

    public void sendToCluster(DataBatch b) {
        Cluster.getInstance().addBatchToProcess(b);
    }

    public LinkedList<DataBatch> getProcessingBatches() {
        return processingBatches;
    }

    public Type getType() {
        return type;
    }

    public AtomicInteger getTicksWorked() {
        return tickCounter;
    }

    public void incrementTick(int toAdd) {
        int curr = tickCounter.get();
        while (!tickCounter.compareAndSet(curr, curr + toAdd)) {
            curr = tickCounter.get();
        }
    }
}

