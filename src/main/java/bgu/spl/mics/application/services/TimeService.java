package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TickBroadcast;

import java.sql.Time;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService  {
	private int spd;
	private int drt;
	private AtomicInteger tickCount;

	public TimeService(int speed,int duration){
		super("TimeService");
		spd=speed;
		drt=duration;
		tickCount = new AtomicInteger(0);
	}

	public int getTickCount() {
		return tickCount.get();
	}

	public int getDuration() {
		return drt;
	}

	@Override
	protected void initialize() {
		// register timeService so that it can send TickBroadcasts
		MessageBusImpl.getInstance().register(this);
		subscribeBroadcast(TickBroadcast.class,b->{
//			System.out.println("Tick has passed at: "+ Time.valueOf(LocalTime.now()).toString());
			if (tickCount.get()==drt)
				return;
			(new Future<>()).get((long)spd, TimeUnit.MILLISECONDS);
			int f;
			do{
				f=tickCount.get();
			}while (!tickCount.compareAndSet(f,f+1));
			MessageBusImpl.getInstance().sendBroadcast(new TickBroadcast());
		});
//		System.out.println("Tick initialized \\TimeService initialize");
		MessageBusImpl.getInstance().sendBroadcast(new TickBroadcast());
//		System.out.println("Tick sent broadcast \\TimeService initialize");
		// how to make sure TickBroadcast is sent?
		// do we need to create an event tick?
	}

}
