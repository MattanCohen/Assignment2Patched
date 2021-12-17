package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.application.objects.Model;

/*
    Sent by the Student, this event is at the core of the system. It will
    be processed by one of the GPU microservices. diagram:
    -student send msgBus data to process
    -msgBus send data to GPU
    -GPU divides data into dataBatches each containing 1000 samples
    -while (theres data not processed):
        -if (GPU has room according to its type to store another dataBatch):
            -send cluster dataBatch (send cluster request to use CPU)
    -set TrainModelEvent result: complete.

 */
public class TrainModelEvent implements Event<Boolean> {

    //set result=true when all data of this event was processed
    Future<Boolean> result;
    Model model;

    public TrainModelEvent(Model _model){
        model=_model;
        result=new Future<Boolean>();
    }

    public Model getModel() {
        return model;
    }

}
