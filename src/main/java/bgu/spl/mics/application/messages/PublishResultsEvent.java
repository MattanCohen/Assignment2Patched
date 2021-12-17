package bgu.spl.mics.application.messages;
import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.application.objects.Model;


/*
    -student sends this event
    -msgBus assigns conference to handle
    -conference collects model names of successful models from students
 */


public class PublishResultsEvent implements Event<Integer> {
    //will hold 1 only if event is finished
    Future<Integer> result;

    // model sent to conference to be published
    Model model;

    public PublishResultsEvent(Model _model){
        result = new Future<>();
        model = _model;
    }

    public Model getModel(){return model;}

}
