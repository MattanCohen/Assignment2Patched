package bgu.spl.mics.application.messages;
import bgu.spl.mics.Broadcast;

/*
    -send broadcast on each tick (each second calculated by TimeService)
    *used for timing conferences publications and processing by GPUs and CPUs
 */

public class TickBroadcast implements Broadcast {
}
