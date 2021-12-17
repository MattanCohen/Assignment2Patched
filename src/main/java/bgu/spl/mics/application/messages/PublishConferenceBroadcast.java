package bgu.spl.mics.application.messages;
import bgu.spl.mics.Broadcast;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.Model;

import java.util.LinkedList;

import java.util.LinkedList;
/*
    -sent by conference (at a set time according to time ticks) to msgBus
    -msgBus broadcasts all collected results to all students
    -unregister conference
 */


public class PublishConferenceBroadcast implements Broadcast {
    // good models sent to conference to be published
    LinkedList<Model> publishedModels;
    Model publishModel;

    public  PublishConferenceBroadcast(Model _publishModel){
        publishModel=_publishModel;
    }

    public PublishConferenceBroadcast(LinkedList<Model> _publishedModels){
        publishedModels = _publishedModels;
    }

    public Model getPublishModel() {
        return publishModel;
    }

    public LinkedList<Model> getPublishedModels() {
        return publishedModels;
    }
}
