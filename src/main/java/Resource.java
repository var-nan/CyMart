import classes.Constants;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.zookeeper.*;

import java.io.IOException;

import static classes.Constants.MAIN_SCRIPT;
import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;


/**
 * @author nandhan, Created on 09/12/23
 */
public class Resource {

    private ZooKeeper zooKeeper;

    private ResourceAD resourceAD;

    private Watcher taskWatcher; // should watch the tasks associated to it.

    private Watcher resourceWatcher;

    private volatile boolean assigned;

    private String clientAddress;

    private String machinePath;

    private volatile double taskDuration;

    private volatile boolean completed;
    // TODO: study more about the security manager and implement it.


    Resource() {
        // connect to zookeeper and place ad
        try {
            this.resourceAD = new ResourceAD(); // TODO initialize object.

            this.zooKeeper = new ZooKeeper("localhost", Constants.ZK_TIMEOUT, null);

            this.resourceWatcher = new Watcher() {
                // sets watch on /resources/resource-id
                @Override
                public void process(WatchedEvent watchedEvent) {
                    switch (watchedEvent.getType()) {
                        case NodeDataChanged: {
                            // new job is assigned to this machine
                            try {
                                var jobAddress = zooKeeper.getData(machinePath, true, null);
                                clientAddress = new String(jobAddress);
                                assigned = true;
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
                                assigned = true;

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
            var bytes = SerializationUtils.serialize(resourceAD);
            this.machinePath = this.zooKeeper.create(Constants.ZK_RESOURCES_PATH+"machine-",
                    bytes, OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            // TODO set watcher.

            this.zooKeeper.exists(this.machinePath, resourceWatcher );


        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }


    class SandboxThread implements Runnable {

        // time it?
        @Override
        public void run() {

            // set completed to true
            double start = System.currentTimeMillis();

            // run program

            double end = System.currentTimeMillis();


            taskDuration = (end-start)/1e6; // TODO correct this.

            completed = true;

        }
    }


    private void start() {

        while(true) {

            while (!assigned && clientAddress != null)
                Thread.onSpinWait();

            // assigned to a job, connect to client to receive script
            FileTransfer.receiveScript(MAIN_SCRIPT, clientAddress);

            Thread workThread = new Thread(new SandboxThread());
            workThread.start();

            // wait for thread to complete
            while (!completed)
                Thread.onSpinWait();

            // reset boolean flags.
            assigned = false;
            completed = false;

            // send final result to the client.

            Thread main = Thread.currentThread();

        }




    }
}
