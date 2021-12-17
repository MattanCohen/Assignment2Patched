package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

import javax.print.attribute.standard.MediaSize;
import java.awt.*;
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

    public Boolean isTrained(Model model){return model.getStatus().toString()=="Trained";}
    public Boolean isPreTrained(Model model){return model.getStatus().toString()=="PreTrained";}
    public Boolean isTested(Model model){return model.getResult().toString()!="None";}

    @Override
    protected void initialize() {
        // register to message bus
        MessageBusImpl.getInstance().register(this);
        // Add message+callback to subscriptions
        subscribeBroadcast(PublishConferenceBroadcast.class, b->{
            LinkedList<Model> publishedModels = b.getPublishedModels();
            for(Model m:publishedModels) {
                Student modelStudent = m.getStudent();
                if (std == modelStudent) { std.publicationsIncrement(); }
                else { std.papersReadIncrement(); }
            }

        });
        subscribeBroadcast(TickBroadcast.class,b->{
            try {
                for(Model m: noneTestedModels) {
                    if (isTested(m)) {
                        if (m.getResult() == Model.Result.Good) {
                            MessageBusImpl.getInstance().sendEvent(new PublishResultsEvent(m));
                        }
                        noneTestedModels.remove(m);
                    }
                }
                for (Model m : noneTrainedModels) {
                    if (isTrained(m)) {
                        MessageBusImpl.getInstance().sendEvent(new TestModelEvent(m));
                        noneTrainedModels.remove(m);
                        noneTestedModels.add(m);
                    } else if (isPreTrained(m)) {
                        m.setStatus(Model.Status.Training);
                        MessageBusImpl.getInstance().sendEvent(new TrainModelEvent(m));
                    }
                }
            } catch (ConcurrentModificationException e){}
        });
    }
}
