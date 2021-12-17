package bgu.spl.mics.application;
import bgu.spl.mics.Future;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import bgu.spl.mics.application.services.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;

/** This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {
    public static void main(String[] args) {
//        String inputFilePath=args[0];
        String inputFilePath = "C:\\Caze Mattan\\University\\2nd year\\3rd semester\\courses\\SPL\\Project2\\Assignment\\Moodle files\\Assignment2Patched\\example_input.json ";

        JsonObject inputJson = readJsonFile(inputFilePath);
        Student[] students = extractStudentList(inputJson);
        // gets list of models for corresponding student index
        Model[][] modelMatrix = extractModelMatrix(inputJson, students);
        Cluster cluster = Cluster.getInstance();

        GPU[] gpus = extractGPUList(inputJson, cluster);
        CPU[] cpus = extractCPUList(inputJson, cluster);

        cluster.setCPUs(cpus);
        cluster.setGPUs(gpus);

        ConfrenceInformation[] conferences = extractConferenceList(inputJson);
        // global variables relevant for time service
        int spd = inputJson.get("TickTime").getAsInt();
        int drt = inputJson.get("Duration").getAsInt();

        MessageBusImpl messageBus = MessageBusImpl.getInstance();

        /*
        summary:
            students - array of students
            modelMatrx - array students.size x student[i].numOfModels
            cluster - the cluster
            gpus - array of gpus
            cpus - array of cpus
            conferences - array of confrenceInformation
            spd - timeService speed
            drt - timeService duration
         */

        LinkedList<StudentService> studentServices = new LinkedList<StudentService>();
        LinkedList<GPUService> gpuServices = new LinkedList<GPUService>();
        LinkedList<CPUService> cpuServices = new LinkedList<CPUService>();
        LinkedList<ConferenceService> conferenceServices = new LinkedList<ConferenceService>();
        LinkedList<Thread> threads = new LinkedList<>();
        //create time service
        TimeService time = new TimeService(spd, drt);
        Integer i=1;
        //create student services
        for (int s = 0; s < modelMatrix.length; s++) {
            LinkedList<Model> models = new LinkedList<>();
            for (int m = 0; m < modelMatrix[s].length; m++)
                models.add(modelMatrix[s][m]);
            StudentService currServ = new StudentService(i.toString(),students[s], models);
            i++;
            studentServices.add(currServ);
            threads.add(new Thread(currServ));
        }
        i=1;
        //create cpu services
        for (CPU c : cpus) {
            CPUService service = new CPUService(i.toString(),c);
            i++;
            cpuServices.add(service);
            threads.add(new Thread(service));
        }
        //create gpu services
        i = 1;
        for (GPU e : gpus) {
            GPUService service = new GPUService(i.toString(), e);
            i++;
            gpuServices.add(service);
            threads.add(new Thread(service));
        }
        i=1;
        //create confrences services
        for (ConfrenceInformation c : conferences) {
            ConferenceService service = new ConferenceService(i.toString(),c);
            i++;
            conferenceServices.add(service);
            threads.add(new Thread(service));
        }

        //prepare all models to test
//        for (StudentService s : studentServices) {
//            for (Model m : s.getNoneTrainedModels()) {
//                messageBus.sendEvent(new TrainModelEvent(m));
//            }
//        }
//        System.out.println("everyone subscribed");
        //start the clock
        for (Thread t : threads)
            t.start();
        Thread f=new Thread(time);
        threads.add(f);
        threads.get(threads.indexOf(f)).start();

        //wait for spd*duration
        (new Future<>()).get(spd*drt, TimeUnit.MILLISECONDS);

        // calculate statistics
        cluster.statistics();

        //if time to terminate has come, unregister all students
        for (StudentService s : studentServices)
            MessageBusImpl.getInstance().unregister(s);
        for (GPUService g : gpuServices)
            MessageBusImpl.getInstance().unregister(g);
        for (CPUService c : cpuServices)
            MessageBusImpl.getInstance().unregister(c);
//        for (ConferenceService cs : conferenceServices)
//            MessageBusImpl.getInstance().unregister(cs);

        System.out.println("cpu time from cluster "+cluster.getTimeCPU());
        System.out.println("cpu amount of data from cluster "+cluster.getDataCPU());
        System.out.println("gpu time from cluster "+cluster.getTimeGPU());
        System.out.println("gpu models trained:");
        for (String m: cluster.getTrainedModelsNames())
        System.out.println("        "+m);
        //create json output file
        JsonObject outputJson = new JsonObject();
        outputJson.add("students", createStudentsJA(students, modelMatrix));
        outputJson.add("conferences", createConferencesJA(conferences));
        outputJson.addProperty("cpuTimeUsed", cluster.getTimeCPU());
        outputJson.addProperty("gpuTimeUsed", cluster.getTimeGPU());
        outputJson.addProperty("batchesProcessed", cluster.getDataCPU());
        // create output file
        String outputString = outputJson.toString();
        try {
            FileWriter writer = new FileWriter("output.txt");
            writer.write(outputString);
            writer.close();
        } catch (IOException e) {
        }
        //stop all threads
        for (Thread t : threads)
            t.stop();
    }
    // runSystem (prepare threads and events needed to be utilized

    // collect data and prepare output file


    /******************* Functions to create output Json from objects ****************************************/
    public static JsonObject createModelJ(Model model){
        JsonObject modelJ = new JsonObject();
        modelJ.addProperty("name",model.getName());
        JsonObject dataJ=new JsonObject();
        dataJ.addProperty("type",model.getData().getType().toString());
        dataJ.addProperty("size",((Integer)(model.getData().getSize())).toString());
        modelJ.add("data",dataJ);
        modelJ.addProperty("status",model.getName());
        modelJ.addProperty("results",model.getResult().toString());
        return modelJ;
    }
    public static JsonArray createModelJA(Model[] modelList){
        JsonArray arr=new JsonArray();
        for(Model c: modelList)
            arr.add(createModelJ(c));
        return arr;
    }
    public static JsonObject createStudentsJ(Student student,Model[]modelList){
        JsonObject studentJ=new JsonObject();
        studentJ.addProperty("name",student.getName());
        studentJ.addProperty("department",student.getDepartment());
        studentJ.addProperty("status",student.getStatus().toString());
        studentJ.addProperty("publications",((Integer)(student.getPublications())).toString());
        studentJ.addProperty("papersRead",((Integer)student.getPapersRead()).toString());
        studentJ.add("trainedModels",createModelJA(modelList));
        return studentJ;
    }
    public static JsonArray createStudentsJA(Student[] students, Model[][] modelMatrix){
        JsonArray arr=new JsonArray();
        for (int i=0; i< students.length; i++){
            arr.add(createStudentsJ(students[i],modelMatrix[i]));
        }
        return arr;
    }
    public static JsonArray createModelJA(LinkedList<Model> modelList){
        JsonArray arr=new JsonArray();
        for(Model c: modelList)
            arr.add(createModelJ(c));
        return arr;
    }
    public static JsonObject createConferenceJ(ConfrenceInformation inf){
        JsonObject conferenceJ=new JsonObject();
        conferenceJ.addProperty("name",inf.getName());
        conferenceJ.addProperty("date",((Integer)inf.getDate()).toString());
        conferenceJ.add("publications:",createModelJA(inf.getModelsToPublish()));
        return conferenceJ;
    }
    public static JsonArray createConferencesJA(ConfrenceInformation[] conferences){
        JsonArray arr=new JsonArray();
        for (ConfrenceInformation c: conferences)
            arr.add(createConferenceJ(c));
        return arr;
    }

/******************* Functions to Extract objects from Input File **************************************/
    /**
     * gets a input json path and returns a Json object
     * */
    private static JsonObject readJsonFile(String inputFilePath) {
        try {

            JsonParser parser = new JsonParser();
            FileReader jsonReader = new FileReader(inputFilePath);
            JsonObject inputJson = parser.parse(jsonReader).getAsJsonObject();

            return inputJson;
        } catch (FileNotFoundException e) {System.out.print("File not found"); }

        return null;
    }

    /**
     * creates a list of Students given a Json array of Students (models are built at later Stage
     * */
    private static Student[] extractStudentList(JsonObject inputJson){
        JsonArray studentJArray = inputJson.getAsJsonArray("Students");
        Student[] students = new Student[studentJArray.size()];
        for( int i=0; i<studentJArray.size(); i++) {
            JsonObject studentJson = studentJArray.get(i).getAsJsonObject();
            // extract Student fields
            String name = studentJson.get("name").getAsString();
            String department = studentJson.get("department").getAsString();
            Student.Degree status;
            if (studentJson.get("status").getAsString()=="MSc")
                status=Student.Degree.MSc;
            else
                status= Student.Degree.PhD;
            // construct object and add to array
            students[i] = new Student(name,department,status);
        }
        return students;
    }

    /**
     * create model list of a single student
     * */
    private static Model[] extractModelList(JsonArray modelJArray, Student linkedStudent){
        Model[] modelList = new Model[modelJArray.size()];
        for(int i=0; i<modelJArray.size();i++) {
            JsonObject modelJson = modelJArray.get(i).getAsJsonObject();
            // create data object linked with Model
            Data.Type data=Data.Type.Images;
            if (modelJson.get("type").getAsString().equals("Text"))
                data=Data.Type.Text;
            if (modelJson.get("type").getAsString().equals("Tabular"))
                data=Data.Type.Tabular;
            Data modelData = new Data(data,modelJson.get("size").getAsInt());
            modelList[i] = new Model(modelJson.get("name").getAsString(),modelData,linkedStudent);
        }

        return modelList;
    }
    /**
     * create matrix of models based on student list and input json
     * */
    private static Model[][] extractModelMatrix(JsonObject inputJson, Student[] students) {
        Model[][] modelMatrix = new Model[students.length][];

        for(int i=0; i<students.length; i++) {
            JsonArray modelJArray = inputJson.get("Students").getAsJsonArray().get(i).getAsJsonObject().get("models").getAsJsonArray();
            Model[] studentModels = extractModelList(modelJArray,students[i]);
            modelMatrix[i] = studentModels;
        }
        return modelMatrix;

    }

    /**
     * creates a list of GPU's given a Json array of gpuTypes, and Cluster object
     * */
    private static GPU[] extractGPUList(JsonObject inputJson, Cluster cluster){
        JsonArray gpuJArray = inputJson.getAsJsonArray("GPUS");
        GPU[] gpus = new GPU[gpuJArray.size()];
        for(int i=0;i<gpuJArray.size();i++) {
            // need to convert GPUType to enum
            String GPUType = gpuJArray.get(i).getAsString();
            gpus[i] = new GPU(GPU.Type.valueOf(GPUType),cluster);
        }
        return gpus;
    }

    /**
     * creates a list of CPU's given a Json array of cpuCores, and Cluster object
     * */
    private static CPU[] extractCPUList(JsonObject inputJson, Cluster cluster){
        JsonArray cpuCoreJArray = inputJson.getAsJsonArray("CPUS");
        /*
         * Here we can convert the jsonArray to a regular array and sort the values
         * if we want the cpu's to be organized by number of cores
         * */

        CPU[] cpus = new CPU[cpuCoreJArray.size()];
        for(int i=0; i<cpuCoreJArray.size();i++) {
            // jsonElement only contains number of cores
            cpus[i] = new CPU(cpuCoreJArray.get(i).getAsInt(),cluster);
        }
        return cpus;
    }

    /**
     * creates a list of ConferenceInformation given a Json array of conference information
     * */
    private static ConfrenceInformation[] extractConferenceList(JsonObject inputJson) {
        JsonArray conferenceJArray = inputJson.getAsJsonArray("Conferences");
        ConfrenceInformation[] conferences = new ConfrenceInformation[conferenceJArray.size()];
        for(int i=0; i<conferenceJArray.size(); i++) {
            // each array element contains a name and a date
            JsonObject conferenceJson = conferenceJArray.get(i).getAsJsonObject();
            // convert name & date to String and int accordingly
            ConfrenceInformation conference = new ConfrenceInformation(conferenceJson.get("name").getAsString(),conferenceJson.get("date").getAsInt());
            conferences[i] = conference;
        }
        return conferences;
    }
/****************************************************************************************************/

}