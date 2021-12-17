package java;

import bgu.spl.mics.example.ServiceCreator;
import bgu.spl.mics.example.services.ExampleBroadcastListenerService;
import bgu.spl.mics.example.services.ExampleEventHandlerService;
import bgu.spl.mics.example.services.ExampleMessageSenderService;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static org.junit.Assert.*;

public class MicroServiceTest {

    //number of actions for bc listener
    String[] nbcl={"1"};
    //number of actions for event listener
    String[] neh={"1"};
    //make sender publish broadcast
    String[] bc={"broadcast"};
    //make sender publish event
    String[] nbc={"event"};

    //create map to identify the services
    Map<String, ServiceCreator> serviceCreators = new HashMap<>();

    //create broadcast listener named "bcl" to do nbcl actions
    ExampleBroadcastListenerService bcl=new ExampleBroadcastListenerService("bcl",nbcl);
    //create event handler named "eh" to do neh actions
    ExampleEventHandlerService eh=new ExampleEventHandlerService("eh",neh);
    //create message sender named "msbc" to send exampleBroadcast
    ExampleMessageSenderService msbc=new ExampleMessageSenderService("ms",bc);
    //create message sender named "msnbc" to send exampleEvent (not broadcast)
    ExampleMessageSenderService msnbc=new ExampleMessageSenderService("ms",nbc);

    @BeforeAll
    public void setUp() throws Exception{
    }

    @Test
    public void subscribeEvent() {
        new Thread(msbc).start();
        new Thread(bcl).start();
        assertEquals(bcl.getMbt(),0);
    }

    @Test
    public void subscribeBroadcast() {
    }

    @Test
    public void sendEvent() {
    }

    @Test
    public void sendBroadcast() {
    }

    @Test
    public void complete() {
    }

    @Test
    public void initialize() {
    }

    @Test
    public void terminate() {
    }

    @Test
    public void getName() {
    }

    @Test
    public void run() {
    }
}