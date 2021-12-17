package java;
import bgu.spl.mics.application.objects.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import java.util.Queue;

import static org.junit.Assert.*;

public class CPUTest {

    CPU test;
    int cores;
    Cluster cluster;

    Data images;
    int imgsSize;

    Data text;
    int txtSize;

    Data tabular;
    int tblrSize;

    Queue<DataBatch> imgs;
    Queue<DataBatch> txts;
    Queue<DataBatch> tblrs;

    @BeforeAll
    public void setUp() throws Exception {
        cores=5;
        imgsSize=2000;
        txtSize=2000;
        tblrSize=2000;
        cluster=Cluster.getInstance();
        test=new CPU(cores,cluster);
        images=new Data(Data.Type.Images,imgsSize);
        text=new Data(Data.Type.Text,txtSize);
        tabular=new Data(Data.Type.Tabular,tblrSize);
        for (int i=0; i<imgsSize; i+=1000){
            imgs.add(new DataBatch(images,i));
        }
        for (int i=0; i<txtSize; i+=1000){
            txts.add(new DataBatch(text,i));
        }
        for (int i=0; i<tblrSize; i+=1000){
            tblrs.add(new DataBatch(tabular,i));
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @Test
    public void getCores() {
        assertEquals(test.getCores(),cores);
    }

    @Test
    public void addData() {
        //add dataBatch type Image
        test.addData(imgs.peek());
        //add dataBatch type text
        test.addData(txts.peek());
        //add dataBatch type Tabluar
        test.addData(tblrs.peek());
        //make sure everything has been inserted
        assertEquals(3,test.getNumOfBatches());
    }

    @Test
    public void getNumOfBatches() {
        //check for empty CPU
        assertEquals(0,test.getNumOfBatches());
        //add dataBatch type Image
        test.addData(imgs.peek());
        assertEquals(1,test.getNumOfBatches());
        //add dataBatch type text
        test.addData(txts.peek());
        assertEquals(2,test.getNumOfBatches());
        //add dataBatch type Tabluar
        test.addData(tblrs.peek());
        assertEquals(3,test.getNumOfBatches());
    }

    @Test
    public void getCluster() {
        assertEquals(cluster,Cluster.getInstance());
    }

    @Test
    public void processData() {
        //add dataBatch type Image
        DataBatch d=imgs.peek();
        test.addData(d);
        //add dataBatch type text
        DataBatch t= txts.peek();
        test.addData(t);
        //make sure processData works from first to last
        assertEquals(test.processData(),d);
        //make sure processData works second time forward
        assertEquals(test.processData(),t);
    }
}