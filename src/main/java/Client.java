import classes.Constants;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

import static classes.Constants.ZK_CLIENTS_PATH;
import static classes.Constants.ZK_TASKS_PATH;
import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;

/**
 * @author nandhan, Created on 03/12/23
 */
public class Client {

    // should call FileTransfer.java

    private ZooKeeper zooKeeper;

    private Watcher taskWatcher;

    private final String ZK_CONNECT = "";

    private String clientId;

    private Map<String,String> dict;

    private JobAD job;

    private String jobLocation;


    Client() {

        try {
            this.taskWatcher = new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    Event.EventType eventType = watchedEvent.getType();

                    switch (watchedEvent.getType()) {
                        case NodeDataChanged: {
                            // read change
                            readData();
                            break;
                        }
                        default:{
                            System.out.println("Something happened");
                            break;
                        }
                    }
                }
            };

            this.zooKeeper = new ZooKeeper(ZK_CONNECT, Constants.ZK_TIMEOUT, null);

            // create client node in zookeeper
            clientId = this.zooKeeper.create(ZK_CLIENTS_PATH + "client-", InetAddress.getLocalHost().getAddress(),
                    OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

            // establish watch?


        } catch (IOException e) {

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    void submitTask() {
        try {

            var bytes = SerializationUtils.serialize(job);
            var actualPath = this.zooKeeper.create(ZK_TASKS_PATH + "task-",
                    bytes,OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);

            this.jobLocation = actualPath; // TODO check if path is relative or absolute.

            this.zooKeeper.exists(this.jobLocation, taskWatcher);

            System.out.println("Job submitted to zookeeper");

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    void readData() {

        try {
            var data = this.zooKeeper.getData("/tasks/task-",taskWatcher,null); // TODO get acutal path
            this.dict = SerializationUtils.deserialize(data);

        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void start() {

        submitTask();


    }
}
