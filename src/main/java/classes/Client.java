package classes;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import static classes.Constants.OUTPUT_2_FILE;
import static classes.Constants.ZK_TASKS_PATH;
import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;

/**
 * @author nandhan, Created on 03/12/23
 */
public class Client {

    // should call classes.FileTransfer.java

    private ZooKeeper zooKeeper;

    private volatile boolean isAssigned;

    private volatile boolean isCompleted;

    private Watcher taskWatcher;

    private final String ZK_CONNECT = "";

    private String taskPath;

    private Map<String,String> dict;

    private JobAD job;

    private String jobLocation; // in zookeeper znode

    private DTO.ZKObject zkObject;


    Client(String zkConnectString) {

        try {

            isAssigned = false;
            isCompleted = false;

            this.taskWatcher = new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    Event.EventType eventType = watchedEvent.getType();

                    System.out.println("Notification Received");
                    System.out.flush();
                    switch (watchedEvent.getType()) {

                        case NodeDataChanged-> {
                            // read change
                            zkObject = readData();
                            if (zkObject.status == DTO.Status.ASSIGNED)
                                isAssigned = true;
                            else if (zkObject.status == DTO.Status.COMPLETED)
                                isCompleted = true;
                            else {
                                // do something
                            }
                        }
                    }
                }
            };

            this.zooKeeper = new ZooKeeper(zkConnectString, Constants.ZK_TIMEOUT, null);
            System.out.println("Connected to zookeeper");

            //classes.JobAD jobAD = new classes.JobAD();// TODO Fill this.
            //var bytes = SerializationUtils.serialize(jobAD);
            // create client node in zookeeper
            //taskPath = this.zooKeeper.create(ZK_TASKS_PATH + "task-", bytes,
            //        OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

            //zooKeeper.addWatch(taskPath, taskWatcher, AddWatchMode.PERSISTENT);

            // establish watch?


        } catch (IOException e) {

        }
    }

    DTO.ZKObject readData() {

        DTO.ZKObject object = null;
        try {
            var data = this.zooKeeper.getData(this.jobLocation,taskWatcher,null); // TODO get acutal path
            object = SerializationUtils.deserialize(data);
            // start server for the machine to connect.
            System.out.println("Data from server: "+object.status + " MachineID: "+object.machineAddress);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return object;
    }

    void submitTask() {
        try {
            var address = InetAddress.getLocalHost().getHostAddress();
            JobAD ad = new JobAD("nvidia",address);

            var bytes = SerializationUtils.serialize(ad);
            this.jobLocation = this.zooKeeper.create(ZK_TASKS_PATH +"/" + "task-",
                    bytes,OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

            // set watch
            this.zooKeeper.getData(this.jobLocation, taskWatcher, null);

            //zooKeeper.addWatch(jobLocation, taskWatcher, AddWatchMode.PERSISTENT);

            //this.jobLocation = actualPath; // TODO check if path is relative or absolute.

            //this.zooKeeper.exists(this.jobLocation, taskWatcher);

            System.out.println("Job submitted to zookeeper");

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }



    void start() {

        submitTask();

        while(zkObject == null || zkObject.status == DTO.Status.INIT) {
            // do something
            Thread.onSpinWait();
        }

        var machineAddress = zkObject.machineAddress;

        System.out.println("Initiating file transfer");

        // start server for machine
        FileTransfer.sendScript(machineAddress); // TODO


        while(!isCompleted)
            Thread.onSpinWait();

        //
        System.out.println("Initiating .");
        FileTransfer.receiveResult(OUTPUT_2_FILE,machineAddress);

        System.out.println("Received Output.");

    }

    public static void main(String[] args) {
        Client client = new Client("localhost");

        client.start();

    }
}
