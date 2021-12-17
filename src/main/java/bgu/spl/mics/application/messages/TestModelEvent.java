package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.application.objects.Model;


/*
    diagram:
    -student send test model event
    -some GPU receives the event
    -GPU updates the object
    -msgBus sets future={
                            "Good" with probability of 0.6 for MSc and 0.8 for PhD
                            "Bad" else
                        }
 */
public class TestModelEvent implements Event<String> {
    Future<String> result;
    Model model;

    public TestModelEvent (Model _model){
        result=new Future<String>();
        model=_model;
    }

    public Future<String> getResult() {
        return result;
    }

    public Model getModel() {
        return model;
    }

}
