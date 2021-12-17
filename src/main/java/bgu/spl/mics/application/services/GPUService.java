package bgu.spl.mics.application.services;

import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.*;
//import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * GPU service is responsible for handling the
 * {@link TrainModelEvent} and {@link TestModelEvent},
 * in addition to sending the
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {

    private GPU gpu;
    AtomicInteger time;
    private Queue<TrainModelEvent> eventQueue;

    public GPUService(String name,GPU _gpu){
        super("GPU Service - "+name);
        gpu=_gpu;
        time=new AtomicInteger(0);
        eventQueue =new LinkedList<TrainModelEvent>();
    }
    public GPUService(GPU _gpu){
        super("GPUService");
        gpu=_gpu;
        time=new AtomicInteger(0);
        eventQueue =new LinkedList<TrainModelEvent>();
    }



    @Override
    protected void initialize() {
        //register to msgbus
        MessageBusImpl.getInstance().register(this);
        //sub to events with those callbacks \/
        subscribeEvent(TrainModelEvent.class,(newEvent)-> {
            //add event to the events queue
            eventQueue.add(newEvent);
            //if eventqueue is already checked return
            if (eventQueue.size()>1)
                return;
            //else start cheking eventQueue until its empty
            while (eventQueue.size()>0){
                //remove and store the head of eventQueue as "e"
                TrainModelEvent e= eventQueue.remove();
                //get and store e's model as "toTrain"
                Model toTrain=e.getModel();
                //set toTrain to be "Training"
                toTrain.setStatus(Model.Status.Training);
                //make toTrain's data LinkedList of DataBatches with matching indexes
                LinkedList<DataBatch> unprocessedData=gpu.dataToBatches(toTrain.getData());
                int i=0;
                Boolean notTrained=true;
                //while theres some unProcessed and unTrained batches
                while (notTrained){
                    //try to add batch unprocessedData.get(i) if and only if i<unprocessedData
                    if (i<unprocessedData.size() && gpu.addBatch(unprocessedData.get(i))){
                        //if we succeed adding unprocessedData.get(i) then increment i by 1
                        gpu.sendToCluster(unprocessedData.get(i));
                        i++;
                    }
                    notTrained=false;
                    //set processingData as the processingBatches gpu has
                    LinkedList<DataBatch> processingData=gpu.getProcessingBatches();
                    for (int j=0; j<processingData.size(); j++){
                        //check if gpu has processed databatches
                        if (processingData.get(j).isProcessed()){
                            //set toAdd to be the wait time (by gpu type)
                            int toAdd = 0;
                            switch (gpu.getType()) {
                                case RTX3090:
                                    toAdd = 1;
                                    break;
                                case RTX2080:
                                    toAdd = 2;
                                    break;
                                case GTX1080:
                                    toAdd = 4;
                                    break;
                            }
                            //get the time we need to wait for that batch train
                            int needTime = time.get()+toAdd;
                            int diff=needTime-time.get();
                            //for every tick increment gpu's ticks becuase it is doing work
                            while (time.get()<needTime) {
                                run();
                                int prevDiff=diff;
                                diff=needTime-time.get();
                                //if a tick occured increment as mentioned
                                if (prevDiff>diff)
                                    gpu.incrementTick(1);
                            }
                            //batch was processed, remove it from gpu
                            if (j<processingData.size())
                            gpu.removeProcessedBatch(processingData.get(j));
                        }
                        //if theres even 1 databatch unprocessed, keep training and try to add
                        else
                            notTrained=true;
                    }
                    }
                //since toTrain has no unprocessed data set status as Trained
                toTrain.setStatus(Model.Status.Trained);
                //add model to queue of models trained by gpu in gpu
                gpu.addModel(toTrain);
            //now keep checking for next events on TrainedModelQueue
            }
            });
        subscribeEvent(TestModelEvent.class,e->{
            //default model result is bad
            String result="";
            //create random number 0-99
            int random=(int)(100*Math.random());
            //in case student is phd, prob is 0.8 to change to good
            if (e.getModel().getStudent().getStatus()== Student.Degree.PhD){
                if (random<80){
                    e.getModel().setResult(Model.Result.Good);
                    result="Good";
                }
                else{
                    e.getModel().setResult(Model.Result.Bad);
                    result="Bad";
                }
            }
            //prob is 0.6 for MSc student to change to good
            else{
                if (random<60){
                    e.getModel().setResult(Model.Result.Good);
                    result="Good";
                }
                else{
                    e.getModel().setResult(Model.Result.Bad);
                    result="Bad";
                }
            }
            e.getModel().setStatus(Model.Status.Tested);
            MessageBusImpl.getInstance().complete(e,result);
        });
        subscribeBroadcast(TickBroadcast.class,b->{
            int f=time.get();
            while (!time.compareAndSet(f, f+1))
                f=time.get();
        });
    }

}