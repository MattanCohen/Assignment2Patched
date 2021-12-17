package bgu.spl.mics.application.services;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.Model;

import java.util.LinkedList;

/**
 * Conference service is in charge of
 * aggregating good results and publishing them via the {@link },
=======
import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroPair;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.DataBatch;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Conference service is in charge of
 * aggregating good results and publishing them via the {@link PublishConferenceBroadcast},
>>>>>>> b98f4105ef283653f6c7768ead3b27c6e3b1a3e0
 * after publishing results the conference will unregister from the system.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ConferenceService extends MicroService {
    int tickTime;
    ConfrenceInformation conference;
    LinkedList<Model> modelsToPublish;


    public ConferenceService(String name,ConfrenceInformation rhs){
        super("ConferenceService - "+name);
        tickTime=0;
        conference=rhs;
        modelsToPublish=new LinkedList<Model>();
    }
    public ConferenceService(ConfrenceInformation rhs){
        super("ConferenceService");
        tickTime=0;
        conference=rhs;
        modelsToPublish=new LinkedList<Model>();
    }

    @Override
    protected void initialize() {
        //register to msgbus
        MessageBusImpl.getInstance().register(this);
        //sub to events with those callbacks \/
        subscribeEvent(PublishResultsEvent.class,(e)-> {
            Model modelToPublish = e.getModel();
            // add good model to conference
            if(modelToPublish.getResult()== Model.Result.Good)
            {modelsToPublish.add(modelToPublish);}
        });
        subscribeBroadcast(TickBroadcast.class, (b)->{
            tickTime++;
            // if we reach conference date, public the conference
            if (tickTime==conference.getDate()) {
                conference.setModelsToPublish(modelsToPublish);
                PublishConferenceBroadcast pubC = new PublishConferenceBroadcast(modelsToPublish);
                sendBroadcast(pubC);
                // after conference is published we want to unregister
                MessageBusImpl.getInstance().unregister(this);
            }
        });
    }
}
