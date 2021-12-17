package java;
import bgu.spl.mics.application.objects.*;

import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.*;

public class GPUTest {
    GPU gpuTest;
    Data d;
    Student s;
    Model model;
    Cluster cluster;

    @Before
    public void setTest() {
        cluster=Cluster.getInstance();
        gpuTest = new GPU(GPU.Type.RTX2080, cluster);
        d = new Data(Data.Type.Images, 1000);
        s = new Student();
        model = new Model("testModel", d, s);
    }


}
