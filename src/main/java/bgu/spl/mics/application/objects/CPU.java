package bgu.spl.mics.application.objects;

import bgu.spl.mics.MicroPair;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class CPU {

    final int cores;
    // number of ticks required to clear queue
    private AtomicInteger ticksToClearQueue;
    AtomicReference<Queue<MicroPair<Integer,DataBatch>>> data;
    private AtomicInteger ticksWorked;

    /**
     *
     * @param _cores
     * @param _cluster
     * @post data==null
     */
    public CPU(int _cores, Cluster _cluster){
        cores=_cores;
        ticksToClearQueue = new AtomicInteger(0);
        data = new AtomicReference<Queue<MicroPair<Integer,DataBatch>>>(new LinkedList<>());
        ticksWorked=new AtomicInteger(0);
    }

    public int getTicksWorked() {
        return ticksWorked.get();
    }

    /**
     * @return cores
     */
    public int getCores() {
        return cores;
    }

    public DataBatch removeFirstData() {
        //remove first element in without
        Queue<MicroPair<Integer, DataBatch>> with = data.get();
        Queue<MicroPair<Integer, DataBatch>> without = data.get();
        MicroPair<Integer,DataBatch> removed=without.remove();
        while (!data.compareAndSet(without, with)) {
            with = data.get();
            without = data.get();
            removed=without.remove();
        }
        int f=ticksToClearQueue.get();
        while (!ticksToClearQueue.compareAndSet(f,f+removed.second().getTimeToDoBatch(this)))
            f=ticksToClearQueue.get();
        //substract ticks to clear queue accordingly
        //change removed pair's databatch to processed
        removed.second().finishProcessing();


        //return dataBatch removed
        return removed.second();
    }


    /**
     * add data to the collection
     * @param newData
     * @pre data is not in queue this.data
     * @post data is in queue this.data
     */
    public void addData(DataBatch newData){
        //get time needed to calculate data batch
        int timeNeeded=newData.getTimeToDoBatch(this);
        //create copy queue with newData to change to
        Queue<MicroPair<Integer,DataBatch>> without= data.get();
        Queue<MicroPair<Integer,DataBatch>> with= data.get();
        //add pair with amount of time to calculate and the same dataBatch
        with.add(new MicroPair<Integer, DataBatch>(timeNeeded,newData));
        //try to atomically add data and time needed to calculate it:
        while (!data.compareAndSet(without,with)){
            without= data.get();
            with= data.get();
            with.add(new MicroPair<Integer, DataBatch>(timeNeeded,newData));
        }
        // add ticks atomically to clear entire queue
        int before=ticksToClearQueue.get();
        while(!ticksToClearQueue.compareAndSet(before,before+timeNeeded))
            before=ticksToClearQueue.get();
    }

    /**
     * get number of dataBatches in cpu atm
     * @return data.size
     */
    //get number of batches cpu is handling
    public int getNumOfBatches(){
        return data.get().size();
    }


    /** process the first data for calculated time milliseconds and return processed data
     * @return the data processed or null if getNumOfBatches()==0
     * if getNumOfBatches()>0: @post.getNumOfBatches() = @pre.getNumOfBatches() - 1
     * if getNumOfBatches()>0: @post first batch in queue this.data is removed
     */
    synchronized public DataBatch processData() {

        //if theres elements
        if (data.get().size() > 0) {
            //increment tick number cpu worked
            int f = ticksWorked.get();
            while (!ticksWorked.compareAndSet(f, f + 1)) {
                f = ticksWorked.get();
            }
            //(ticksToClearQueue) decrement 1 because cpu worked 1 tick
            int ff;
            do{
                ff=ticksToClearQueue.get();
            }while(!ticksToClearQueue.compareAndSet(ff, ff- 1));

            //if first batch was running for enough ticks remove it from data queue and set as processed
            if (data.get().peek().first() == 0) {
                return removeFirstData();
            }
            else {
                //down 1 tick for first element in data queue
                data.get().peek().setFirst(data.get().peek().first() - 1);
            }

        }
        // default return value
        return null;
    }
    /**
     * return the number of ticks until CPU finishes with all batches
     * */
    public int getTicksToClearQueue(){return ticksToClearQueue.get();}

}
