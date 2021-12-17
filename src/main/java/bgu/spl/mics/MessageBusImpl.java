package bgu.spl.mics;


import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Model;

import java.sql.SQLType;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * The message-bus is a shared object used for communication between
 * micro-services.
 * It should be implemented as a thread-safe singleton.
 * The message-bus implementation must be thread-safe as
 * it is shared between all the micro-services in the system.
 * You must not alter any of the given methods of this interface.
 * You cannot add methods to this interface.
 */
public class MessageBusImpl implements MessageBus {

	private HashMap<Class<? extends Message>, MicroPair<Integer,LinkedList<MicroService>>> messages;
	private HashMap <MicroService,Queue<Message>> microServices;
	private HashMap<Event,Future> eventFutures;

	private static class SingletonHolder{
		private static MessageBusImpl instance=new MessageBusImpl();
	}

	private MessageBusImpl(){
		messages=new HashMap<Class<? extends Message>,MicroPair<Integer, LinkedList<MicroService>>>();
		microServices=new HashMap<MicroService,Queue<Message>>();
		eventFutures=new HashMap<Event,Future>();
	}
	synchronized public static MessageBusImpl getInstance(){return SingletonHolder.instance;}

	/**
	 * Subscribes {@code m} to receive {@link Event}s of type {@code type}.
	 * <p>
	 * @param <T>  The type of the result expected by the completed event.
	 * @param type The type to subscribe to,
	 * @param m    The subscribing micro-service.
	 * @pre m is registered
	 * @post m is subscribed to event
	 */
	@Override
	synchronized public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		//first time show
		if (messages.get(type)==null){
			messages.put(type,new MicroPair<>(0,new LinkedList<MicroService>()));
			for (MicroService M: microServices.keySet())
				if(M==m)
					messages.get(type).second().add(m);
		}
		//not first time show
		else{
			for (MicroService M: microServices.keySet())
				if(M==m)
					messages.get(type).second().add(m);
		}
	}

	/**
	 * Subscribes {@code m} to receive {@link Broadcast}s of type {@code type}.
	 * <p>
	 * @param type     The type to subscribe to.
	 * @param m        The subscribing micro-service.
	 * @pre m is registered
	 * @post m is subscribed to broadcast
	 */
	@Override
	synchronized public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		//first time show
		if (messages.get(type)==null){
			messages.put(type,new MicroPair<>(0,new LinkedList<MicroService>()));
			for (MicroService M: microServices.keySet())
				if(M==m)
					messages.get(type).second().add(M);
		}
		//not firt time show
		else{
			for (MicroService M: microServices.keySet())
				if(M==m)
					messages.get(type).second().add(M);
		}
	}

	/**
	 * Notifies the MessageBus that the event {@code e} is completed and its
	 * result was {@code result}.
	 * When this method is called, the message-bus will resolve the {@link Future}
	 * object associated with {@link Event} {@code e}.
	 * <p>
	 * @param <T>    The type of the result expected by the completed event.
	 * @param e      The completed event.
	 * @param result The resolved result of the completed event.
	 * @pre Future.isDone()==False
	 * @post Future.isDone()==True
	 * @post Future.get()== result
	 */
	@Override
	synchronized public <T> void complete(Event<T> e, T result) {
		eventFutures.get(e).resolve(result);
	}

	/**
	 * Adds the {@link Broadcast} {@code b} to the message queues of all the
	 * micro-services subscribed to {@code b.getClass()}.
	 * <p>
	 * @param b    The message to added to the queues.
	 * @post all subscribed microservices get broadcast
	 */
	@Override
	synchronized public void sendBroadcast(Broadcast b) {
		//if no microservices are subscribed to handle events of type of e
		if(messages.get(b.getClass())==null)
			return;
		if (messages.get(b.getClass()).second().size()==0)
			return;
		// add broadcast to all microservices's queues
		LinkedList<MicroService> servicesList=messages.get(b.getClass()).second();
		for (int i=0; i<servicesList.size(); i++) {
			microServices.get(servicesList.get(i)).add(b);
		}
		//wake up threads sleeping in await
		notifyAll();
	}

	/**
	 * Adds the {@link Event} {@code e} to the message queue of one of the
	 * micro-services subscribed to {@code e.getClass()} in a round-robin
	 * fashion. This method should be non-blocking.
	 * <p>
	 * @param <T>      The type of the result expected by the event and its corresponding future object.
	 * @param e        The event to add to the queue.
	 * @return {@link Future<T>} object to be resolved once the processing is complete,
	 *            null in case no micro-service has subscribed to {@code e.getClass()}.
	 * @pre event isn't queued to any microservices
	 * @inv Future.isDone()==False
	 * @post Future.isDone()==False
	 */
	@Override
	synchronized public <T> Future<T> sendEvent(Event<T> e) {
		if(messages.get(e.getClass())==null)
			return null;
		//if no microservices are subscribed to handle events of type of e
		if (messages.get(e.getClass()).second().size()==0)
			return null;
		// add event to a specific microservice's queue in a round robin matter
		Integer counter = messages.get(e.getClass()).first();
		// if we reached end of the link, round robin, reset counter
		if(counter>=messages.get(e.getClass()).second().size())
			counter=0;
		//add event to the microService we chose round redrobindhoodly
		MicroService microService = messages.get(e.getClass()).second().get(counter);
		counter++;
		microServices.get(microService).add(e);
		//change counter accordingly
		messages.get(e.getClass()).setFirst(counter);
		// add event + future to hashmap of eventFutures
		Future<T> ans=new Future<T>();
		eventFutures.put(e,ans);
		//wake up threads sleeping in await
		notifyAll();
		// return future ans
		return ans;
	}

	/**
	 * Allocates a message-queue for the {@link MicroService} {@code m}.
	 * <p>
	 * @param m the micro-service to create a queue for.
	 * @pre microservice isn't registered
	 * @post microservice is registered
	 *
	 **/
	@Override
	synchronized public void register(MicroService m) {
		if (microServices.get(m)!=null)
			return;
		microServices.put(m,new LinkedList<Message>());
	}

	/**
	 * Removes the message queue allocated to {@code m} via the call to
	 * {@link #register(bgu.spl.mics.MicroService)} and cleans all references
	 * related to {@code m} in this message-bus. If {@code m} was not
	 * registered, nothing should happen.
	 * <p>
	 * @param m the micro-service to unregister.
	 * @post microservice is unregistered
	 * @post microservice's messages queue and all references are cleared
	 */
	@Override
	synchronized public void unregister(MicroService m) {
		m.terminate();
		for (Class<? extends Message> e: messages.keySet())
			messages.get(e).second().remove(m);
		microServices.remove(m);
	}


	/*
	awaitMessage(Microservice m): A Micro-Service calls this method in order to
	take a message from its allocated queue. This method is blocking (waits until
	there is an available message and returns it).
	 */
	/**
	 * Using this method, a <b>registered</b> micro-service can take message
	 * from its allocated queue.
	 * This method is blocking meaning that if no messages
	 * are available in the micro-service queue it
	 * should wait until a message becomes available.
	 * The method should throw the {@link IllegalStateException} in the case
	 * where {@code m} was never registered.
	 * <p>
	 * @param m The micro-service requesting to take a message from its message
	 *          queue.
	 * @return The next message in the {@code m}'s queue (blocking).
	 * @throws InterruptedException if interrupted while waiting for a message
	 *                              to became available.
	 * @pre m is registered
	 * @inv no return while m's message queue is empty
	 * @post m's messages queue size = @pre m's messages queue size - 1
	 *
	 * */
	@Override
	 synchronized public Message awaitMessage(MicroService m) throws InterruptedException {
		if (microServices==null)
			throw new IllegalStateException();
		// checks if microservice is registered
		if(microServices.get(m)==null) {
			throw new IllegalStateException();
		}
		// checks if microservice message queue is empty and blocks if empty
		while (microServices.get(m).size()==0) {
			try {
				wait();
			} catch (InterruptedException e){
				//if thread is interrupted by notifyAll() check if it has a message. if so, return it. else, keep waiting
				if (microServices.get(m).size()>0)
					return microServices.get(m).remove();
			}
			catch (NullPointerException f){
				System.out.println(m.getName()+" got null:");
				System.out.println(f.getMessage());
			}
		}
		return microServices.get(m).remove();
	}
}
