package bgu.spl.mics;
import java.util.*;
import java.util.concurrent.TimeUnit;

/*
 * A Future object represents a promised result - an object that will
 * eventually be resolved to hold a result of some operation. The class allows
 * Retrieving the result once it is available.
 * 
 * Only private methods may be added to this class.
 * No public constructor is allowed except for the empty constructor.
 */


public class Future<T> {
	T result;
	/**
	 * This should be the the only public constructor in this class.
	 * @post result=null;
	 */
	public Future() {
		result=null;
	}

	/**
	 * retrieves the result the Future object holds if it has been resolved.
	 * This is a blocking method! It waits for the computation in case it has
	 * not been completed.
	 * <p>
	 * @return return the result of type T if it is available, if not wait until it is available.
	 */
	synchronized public T get() {
		while (!isDone()){
			try {
				wait();
			}
			catch (InterruptedException e){}
		}
		return result;
	}

	/**
	 * Resolves the result of this Future object.
	 * @param result
	 * @pre result = null
	 * @inv result wasn't yet updated
	 * @post this.result == result
	 */
	synchronized public void resolve (T result) {
		this.result=result;
		notifyAll();
	}
	
	/**
     * @return true if this object has been resolved, false otherwise
     */
	synchronized public boolean isDone() {return result!=null;}

	/**
	 * retrieves the result the Future object holds if it has been resolved,
	 * This method is non-blocking, it has a limited amount of time determined
	 * by {@code timeout}
	 * <p>
	 * @param timeout  the maximal amount of time units to wait for the result.
	 * @param unit    the {@link TimeUnit} time units to wait.
	 * @return return the result of type T if it is available, if not,
	 *            wait for {@code timeout} TimeUnits {@code unit}. If time has
	 *         elapsed, return null.
	 * @pre timeout >= 0
	 * @inv currTime-startTime<=timeout (in unit) // currTime-startTime: time passed from start of function
	 * @return result if(isDone)
	 * @return null if(currTime-startTime>timeout and !isDone)
	 */
	synchronized public T get(long timeout, TimeUnit unit) {
		if (!isDone()){
			try{
				wait(unit.toMillis(timeout));
			}
			catch (InterruptedException e){}
		}
		if (isDone())
			return result;
		return null;
	}

}
