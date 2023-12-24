package classes;

//import UITest.Computation;
//import UITest.Computation2;
import net.openhft.compiler.CompilerUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.zookeeper.*;

import javax.tools.JavaCompiler;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static classes.Constants.ZK_TASKS_PATH;
import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;


/**
 * @author nandhan, Created on 09/12/23
 */
public class Resource {

    private ZooKeeper zooKeeper;

    private MachineAD machineAD;
    private String machineAddress;

    private Watcher taskWatcher; // should watch the tasks associated to it.

    private Watcher machineWatcher; // watch its own znode in zookeeper

    private volatile boolean isAssigned;

    private String clientAddress;
    private String taskID;

    private String machinePath; // machine path in znode

    private volatile boolean completed;
    // TODO: study more about the security manager and implement it.


    Resource(String zkConnectString) {
        // connect to zookeeper and place ad
        try {
            machineAddress = InetAddress.getLocalHost().getHostAddress();
            this.machineAD = new MachineAD(machineAddress); // TODO initialize object.

            this.zooKeeper = new ZooKeeper(zkConnectString, Constants.ZK_TIMEOUT, null);
            System.out.println("Connected to zookeeper");
            this.isAssigned = false;

            this.machineWatcher = new Watcher() {
                // sets watch on /resources/resource-id
                @Override
                public void process(WatchedEvent watchedEvent) {
                    switch (watchedEvent.getType()) {
                        case NodeDataChanged: {
                            // new job is assigned to this machine
                            try {
                                var jobAddress = zooKeeper.getData(machinePath, taskWatcher, null);
                                DTO.ZKObject object = SerializationUtils.deserialize(jobAddress);
                                clientAddress = object.clientAddress;
                                taskID = object.taskID;
                                isAssigned = true;
                            } catch (KeeperException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }
            };

            this.taskWatcher = new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    switch (watchedEvent.getType()) {
                        case NodeCreated: {

                        }
                        case NodeDataChanged: {
                            // got assigned to a task.
                            // read the machine's address and connect to it.

                            try {
                                var address = zooKeeper.getData("/somepaht",true, null);
                                clientAddress = new String(address);
                                isAssigned = true;

                            } catch (KeeperException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            };
            // place ad on zookeeper
            var bytes = SerializationUtils.serialize(machineAD);
            this.machinePath = this.zooKeeper.create(Constants.ZK_MACHINES_PATH +"/machine-",
                    bytes, OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            // TODO set watcher.

            // change to get data.
            this.zooKeeper.getData(this.machinePath, machineWatcher,null);


        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Resource resource = new Resource("localhost");

        resource.start();
    }


    /**
     * Starts the machine after receiving task from classes.Master.
     *
     */
    private void start() {

        System.out.println("Starting the resource");

        while(true) {

            while (!isAssigned || clientAddress == null)
                Thread.onSpinWait();

            System.out.println("Ready to receive job file");

            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // assigned to a job, connect to client to receive script
            FileTransfer.receiveScript(clientAddress);


            Thread workThread = new Thread(new SandboxThread());
            workThread.start();

            //Thread main = Thread.currentThread();

            // wait for worker thread to join master thread.
            try {
                workThread.join();
                //System.out.println("Worker thread finished");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // wait for thread to complete
            while (!completed)
                Thread.onSpinWait();
            // TODO handle exception during script execution.
            // reset boolean flags.

            // write status to zookeeper
            writeStatus();
            System.out.println("Status written to client znode");

            try {
                //System.out.println("Waiting for client to start server.");
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Sending results to the client");
            FileTransfer.sendResult(clientAddress);

            // delete or clear both files?
            FileTransfer.deleteFiles();

            System.out.flush();

            isAssigned = false;
            completed = false;

        }
    }

    private void writeStatus() {
        try {

            if (this.zooKeeper.exists(ZK_TASKS_PATH+"/"+taskID,null) != null) {
                var object = new DTO.ZKObject();
                object.clientAddress = clientAddress;
                object.machineAddress = machineAddress;// TODO
                object.taskID = taskID;
                object.status = DTO.Status.COMPLETED;

                var bytes = SerializationUtils.serialize(object);
                System.out.println("Writing status to "+ZK_TASKS_PATH + "/"+taskID);
                this.zooKeeper.setData(ZK_TASKS_PATH + "/"+ taskID, bytes, -1);
                // this will trigger watches at master and client.

                // TODO enable watcher again.
            } else {
                // throw some exception or contact master
            }

        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }


    class SandboxThread implements Runnable {

        // use a new security manager.

        // time it?
        @Override
        public void run() {

            /*
            try {
                String className = "Compute";
                Path path = Paths.get("./src/main/java/UITest/Compute.java");
                //Charset charset = StandardCharsets.UTF_8;
                String javaCode = Files.readString(path);

                Class<?> aClass = CompilerUtils.CACHED_COMPILER.loadFromJava(className, javaCode);
                Runnable runnable = (Runnable) aClass.newInstance();
                runnable.run();
                System.out.println("Started the script.");

            } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();

            }

             */

            try {
                double start = System.currentTimeMillis();
                var processBuilder = new ProcessBuilder("java", "src/main/java/UITest/Compute.java");
                var process = processBuilder.start();

                process.waitFor();
                //System.out.println("Process completed");
                double end = System.currentTimeMillis();

                double taskDuration = (end - start); // TODO correct this.
                try (
                    BufferedInputStream inputStream =
                            new BufferedInputStream(process.getInputStream(),512);
                    BufferedInputStream errorStream =
                            new BufferedInputStream(process.getErrorStream());

                    BufferedOutputStream bufferedOutputStream =
                            new BufferedOutputStream(new FileOutputStream("output.txt"));
                    PrintStream printStream = new PrintStream(System.out);
                ) {
                    //System.out.println("Printing output from process");
                    byte[] output = inputStream.readAllBytes();
                    //printStream.println(new String(output));
                    //printStream.flush();
                    // write to file.
                    var str = "***********output*******\n";
                    bufferedOutputStream.write(str.getBytes());
                    bufferedOutputStream.write(output);
                    bufferedOutputStream.flush();

                    var errorStr = "***********error*********\n";
                    bufferedOutputStream.write(errorStr.getBytes());
                    output = errorStream.readAllBytes();
                    bufferedOutputStream.write(output);
                    bufferedOutputStream.flush();

                    var time = ("""

                            Execution time of Script is : %.3f ns.
                            Billing amount : $ %.5f 
                            """).formatted(taskDuration, taskDuration * 0.0205);
                    bufferedOutputStream.write(time.getBytes());
                    bufferedOutputStream.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                /*
                System.out.println("printing error from process");
                byte[] error = errorStream.readAllBytes();
                printStream.write(error);
                printStream.flush();

                bufferedOutputStream.write("\nError\n".getBytes());
                bufferedOutputStream.write(error);
                bufferedOutputStream.flush();

                 */

                System.out.println("Completed script. Duration: "+ taskDuration);
                System.out.flush();
                completed = true;

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }



}
