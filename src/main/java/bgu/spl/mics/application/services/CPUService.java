package bgu.spl.mics.application.services;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.CPU;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * CPU service is responsible for handling the
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
/*
 *sole responsibility is to update the time for the CPU
 */

public class CPUService extends MicroService {

    private CPU cpu;
    // number of ticks cpu has used to process batches
    AtomicInteger timeUsed;

    public CPUService(CPU _cpu){
        super("CPUService");
        cpu=_cpu;
        timeUsed = new AtomicInteger(0);
    }

    public CPUService(String name,CPU _cpu){
        super("CPU Service - "+name);
        cpu=_cpu;
        timeUsed = new AtomicInteger(0);
    }

    @Override
    protected void initialize() {
        // register CPU
        MessageBusImpl.getInstance().register(this);
        // Add tick and callback to subscriptions
        subscribeBroadcast(TickBroadcast.class, b-> {
            // if CPU has batches to process
            if(cpu.getTicksToClearQueue()>0) {
                //process the batches
                if (cpu.getTicksToClearQueue()>0){
                    cpu.processData();
                }
            }
        });
    }
}
