import classes.Constants;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * @author nandhan, Created on 03/12/23
 */
public class Master {

    private ZooKeeper zooKeeper;

    private Watcher tasksWatcher;

    private Watcher resourceWatcher;

    private Map<String,String> matching;


    /**
     * master will assign a task to the resource, by writing data to the assign/resoruce/{task_id}.
     * master writes the resource ip address to the task.
     * resource then get the task and connect to the client's server and gets the job script.
     */


    Master() {

        try {

            matching = new HashMap<>();

            this.resourceWatcher = new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    switch (watchedEvent.getType()) {
                        case NodeDeleted: {
                            // resource is out of system.
                            // if it is currently executing any tasks,
                            // redirect task to another machine.

                            break;
                        }
                        case NodeCreated: {
                            // add it to the system.
                        }
                    }
                }
            };

            this.tasksWatcher = new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    switch (watchedEvent.getType()) {
                        case NodeCreated: {
                            // new task is added to system. assign it to any resource
                            try {
                                var children = zooKeeper.getChildren("tasks/",tasksWatcher);

                                // sort reverse
                                children.sort(Comparator.reverseOrder());

                                children.forEach(
                                        (x) -> {
                                            if (!matching.containsKey(x)) {
                                                addTask(x);
                                            }
                                        }
                                );
                            } catch (KeeperException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }


                        }
                    }
                }
            };

            this.zooKeeper = new ZooKeeper("localhost", Constants.ZK_TIMEOUT, tasksWatcher);


            // create master node
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void addTask(String taskId) {

        // new task.

        try {
            var data = this.zooKeeper.getData("/tasks/"+taskId, true, null);
            ClassAD task = SerializationUtils.deserialize(data);


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    private void getResource(ClassAD ad) {

    }

    public static void main(String[] args ) {
        SecurityManager securityManager = System.getSecurityManager();
        System.out.println(System.getProperty("java.version"));
    }
}
