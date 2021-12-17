package bgu.spl.mics.application.objects;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Cluster {

	private GPU[] gpus;
	private CPU[] cpus;
	// make sure GPU updates cluster each time a model is trained
	private LinkedList<String> trainedModelsNames;
	// total number of data batches
	private int dataCPU;
	private int timeCPU;
	private int timeGPU;

	private static class SingletonHolder{
		private static Cluster instance=new Cluster();
	}
	/**
	 * Retrieves the single instance of this class.
	 */
	synchronized public static Cluster getInstance(){return Cluster.SingletonHolder.instance;}

	private Cluster(){
		trainedModelsNames = new LinkedList<>();
	}

	public void setGPUs(GPU[]_gpus){gpus=_gpus;}
	public void setCPUs(CPU[]_cpus){cpus=_cpus;}

	//add to optimal cpu the data. it will be processed with ticks
	synchronized public void addBatchToProcess(DataBatch toProcess){
//		System.out.println("Cluster recieved batch index: "+toProcess.getStart_index());
		CPU work=getOptimalCPU(toProcess);
		work.addData(toProcess);
	}

	public void statistics(){
		timeCPU=0;
		dataCPU=0;
		timeGPU=0;
		trainedModelsNames=new LinkedList<String>();
		for (CPU c : cpus){
			timeCPU+=c.getTicksWorked();
			dataCPU+=c.getTicksWorked();
		}
		for (GPU g : gpus){
			timeGPU+=g.getTicksWorked().get();
			trainedModelsNames.addAll(g.getNamesModelsTrained());
		}
	}

	/**
	 * based on the batch data type,
	 * find the best CPU to add to batch list
	 * */
	public CPU getOptimalCPU(DataBatch dataBatch) {
		int minIndex = 0;
		int cores = cpus[0].getCores();
		int minNewTicksToClearQueue = cpus[0].getTicksToClearQueue()+dataBatch.getTimeToDoBatch(cpus[0]);

		// get the core that will need the least ticks to clear queue with new batch
		for(int i=1; i<cpus.length; i++) {
			cores = cpus[i].getCores();
			// calculate number of ticks with new batch added
			int newTicksToClearQueue = cpus[i].getTicksToClearQueue()+dataBatch.getTimeToDoBatch(cpus[i]);
			// update fields with better CPU
			if (newTicksToClearQueue<minNewTicksToClearQueue) {
				minIndex=i;
				minNewTicksToClearQueue=newTicksToClearQueue;
			}
		}
		// return the optimal CPU for the job
		return cpus[minIndex];
	}

	public int getTimeCPU() {
		return timeCPU;
	}


	public int getTimeGPU() {
		return timeGPU;
	}

	public int getDataCPU() {
		return dataCPU;
	}

	public LinkedList<String> getTrainedModelsNames() {
		return trainedModelsNames;
	}

	public void addTrainedModel(String name){
		trainedModelsNames.add(name);
	}

}
