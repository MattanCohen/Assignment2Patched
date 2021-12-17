package java;
import bgu.spl.mics.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import static org.junit.Assert.*;



public class MessageBusImplTest {

    //make class < ? extends Event<T> >
    class EventInt implements Event<Integer> {
        public Integer f;
        public EventInt(){
        }
        public EventInt(int F){
            f=F;
        }
    }

    //marker interface to make class < ? extends Event<T> >
    interface BroadcastInt extends Broadcast {
    }

    MessageBusImpl msgBus;
    MicroService m;

    @Before
    public void setUp() throws Exception {
        msgBus=MessageBusImpl.getInstance();
        m=new MicroService("") {
            @Override
            protected void initialize() {}
        };
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @Test
    public void subscribeEvent(){
        //register m to msgBus
        msgBus.register(m);
        //create new event
        EventInt e=new EventInt() {};
        //try to sub m in msgbus for EventInt events
        msgBus.subscribeEvent(EventInt.class,m);
        //set the event of type EventInt to Future object
        Future<Integer> futureObject=(Future<Integer>)msgBus.sendEvent(e);
        try{
            //since m's the only MS in msgBus, it should pull m's message
            assertEquals(msgBus.awaitMessage(m),e);
        }
        catch (InterruptedException E){
            System.out.println("InterruptedException errupted, subscribe event in message bus impl test");
        }
    }

    @Test
    public void subscribeBroadcast() {
        //register m to msgBus
        msgBus.register(m);
        //create new broadcast
        BroadcastInt b=new BroadcastInt() {};
        //try to sub m in msgbus for BroadcastInt broadcasts
        msgBus.subscribeBroadcast(BroadcastInt.class,m);
        //send broadcast to all microservices who's subed to BroadcastInt
        msgBus.sendBroadcast(b);
        try{
            //since m's the only MS in msgBug, it should receive the broadcast
            assertEquals(msgBus.awaitMessage(m),b);
        }
        catch (InterruptedException E){
            System.out.println("InterruptedException errupted, subscribe broadcast in message bus impl test");
        }
    }

    @Test
    public void complete() {
        //register m to msgBus
        msgBus.register(m);
        msgBus.subscribeEvent(EventInt.class,m);
        //create a new EventInt
        EventInt e=new EventInt();
        //create a new future object representing the answer of send event
        Future<Integer> futureObject=(Future<Integer>)msgBus.sendEvent(e);
        //change e's result to 7
        Integer h=7;
        msgBus.complete(e,h);
        //make sure future's value (futureObject represents e's result) is changed to h
        assertEquals(futureObject.get(),h);
    }

    @Test
    public void sendBroadcast() {
        //register m to msgBus
        msgBus.register(m);
        //create new broadcast
        BroadcastInt b=new BroadcastInt() {};
        //sub m in msgbus for BroadcastInt broadcasts
        msgBus.subscribeBroadcast(BroadcastInt.class,m);
        //try to send broadcast to all microservices who's subed to BroadcastInt
        msgBus.sendBroadcast(b);
        try{
            //since m's the only MS in msgBug, it should receive the broadcast
            assertEquals(msgBus.awaitMessage(m),b);
        }
        catch (InterruptedException E){
            System.out.println("InterruptedException errupted, subscribe broadcast in message bus impl test");
        }
    }

    @Test
    public void sendEvent()  {
        //register m to msgBus
        msgBus.register(m);
        //create new event
        EventInt e=new EventInt() {};
        //sub m in msgbus for EventInt events
        msgBus.subscribeEvent(EventInt.class,m);
        //try to send the event of type EventInt to some microservice
        Future<Integer> futureObject=(Future<Integer>)msgBus.sendEvent(e);
        try{
            //since m's the only MS in msgBus, it should pull m's message
            assertEquals(msgBus.awaitMessage(m),e);
        }
        catch (InterruptedException E){
            System.out.println("InterruptedException errupted, subscribe event in message bus impl test");
        }
    }

    @Test
    public void register() {
        //try to register m to msgBus
        msgBus.register(m);
        EventInt e=new EventInt() {};
        msgBus.subscribeEvent(EventInt.class,m);
        msgBus.sendEvent(e);
        try{
            //if m registered msgBus, it should pull m's message
            assertEquals(msgBus.awaitMessage(m),e);
        }
        catch (InterruptedException E){
            System.out.println("InterruptedException errupted, subscribe event in message bus impl test");
        }
    }

    @Test
    public void unregister() {
        //register m to msgBus
        msgBus.register(m);
        EventInt e=new EventInt() {};
        msgBus.subscribeEvent(EventInt.class,m);
        msgBus.sendEvent(e);
        //re-register m to msgBus
        msgBus.unregister(m);
        msgBus.register(m);
        try{
            //make sure m's message queue is null
            assertNull(msgBus.awaitMessage(m));
        }
        catch (InterruptedException E){
            System.out.println("InterruptedException errupted, subscribe event in message bus impl test");
        }
    }

    @Test (expected = InterruptedException.class)
    public void awaitMessage() throws Exception {
        msgBus.register(m);
        EventInt e=new EventInt() {};
        msgBus.subscribeEvent(EventInt.class,m);
        msgBus.sendEvent(e);
        try{
            //if await message works, it should pull m's message
            assertEquals(msgBus.awaitMessage(m),e);
        }
        catch (InterruptedException E){
            System.out.println("InterruptedException errupted, subscribe event in message bus impl test");
        }
        msgBus.unregister(m);
        msgBus.awaitMessage(m); //should throw InterruptedException and test is expecting it to finish
    }

}