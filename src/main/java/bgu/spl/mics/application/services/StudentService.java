package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

import javax.print.attribute.standard.MediaSize;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.Queue;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;
//import com.sun.org.apache.xpath.internal.operations.Mod;

import java.util.LinkedList;

/*
 * Student is responsible for sending the {@link TrainModelEvent},
 * {@link TestModelEvent} and {@link PublishResultsEvent}.
 * In addition, it must sign up for the conference publication broadcasts.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class StudentService extends MicroService {

    Student std;
    LinkedList<Model> noneTrainedModels;
    LinkedList<Model> noneTestedModels;

    public StudentService(String name,Student student, LinkedList<Model> _models){
        super("StudentService Service - "+name);
        std=student;
        noneTrainedModels=_models;
        noneTestedModels=new LinkedList<>();
    }

    public StudentService(Student student, LinkedList<Model> _models){
        super("StudentService");
        std=student;
        noneTrainedModels=_models;
        noneTestedModels=new LinkedList<>();
    }

    public LinkedList<Model> getNoneTrainedModels() {
        return noneTrainedModels;
    }

    public Boolean isTrained(Model model){return model.getStatus().toString()=="Trained";}
    public Boolean isPreTrained(Model model){return model.getStatus().toString()=="PreTrained";}
    public Boolean isGood(Model model){return model.getResult().toString()!="None";}
    @Override
    protected void initialize() {
        // register to message bus
        MessageBusImpl.getInstance().register(this);
        // Add message+callback to subscriptions
        subscribeBroadcast(PublishConferenceBroadcast.class, b->{
            LinkedList<Model> publishedModels = b.getPublishedModels();
            // each model in the conference changes Student
            for(Model m:publishedModels) {
                // Student has another published paper or read another paper
                if (m.getStudent() == std) {
                    std.publicationsIncrement();
                }
                else {
                    std.papersReadIncrement();
                }
            }
        });
        subscribeBroadcast(TickBroadcast.class,b->{
            try {
                for (Model m : noneTrainedModels) {
                    if (isTrained(m)) {
                        noneTrainedModels.remove(m);
                        MessageBusImpl.getInstance().sendEvent(new TestModelEvent(m));
                    } else if (isPreTrained(m)) {
                        m.setStatus(Model.Status.Training);
                        MessageBusImpl.getInstance().sendEvent(new TrainModelEvent(m));
                    }
                }
            }catch (ConcurrentModificationException e){}
                for (Model m: noneTestedModels){
                    if (isGood(m)) {
                        noneTestedModels.remove(m);
                        if (m.getResult()== Model.Result.Good)
                            MessageBusImpl.getInstance().sendEvent(new PublishResultsEvent(m));
                    }
                }

        });
    }
}
