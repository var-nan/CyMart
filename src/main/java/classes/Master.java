package classes;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static classes.Constants.ZK_MACHINES_PATH;
import static classes.Constants.ZK_TASKS_PATH;
import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;

/**
 * @author nandhan, Created on 03/12/23
 */
public class Master {

    private ZooKeeper zooKeeper;

    private Watcher tasksWatcher;

    private Watcher machineWatcher;

    private volatile Map<String,String> matching; // Match machine with a task. (taskId, MachineID)

    private volatile Map<String, MachineAD> machines;

    private volatile Map<String, JobAD> tasks;




    /**
     * master will assign a task to the resource, by writing data to the assign/resoruce/{task_id}.
     * master writes the resource ip address to the task.
     * resource then get the task and connect to the client's server and gets the job script.
     */

    /*
    classes.Master wil assign the tasks to the resoruce, by writing the object to client and resource.
    classes.Client then sets up the server and ready to accept connection from resource.
    resource connects with client and receives the Task file and executes the script
    with a security manager.
     */


    Master(String zkConnectString) {

        try {

            matching = new HashMap<>();
            machines = new HashMap<>();
            tasks = new HashMap<>();

            this.machineWatcher = new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    switch (watchedEvent.getType()) {
                        case NodeChildrenChanged-> {
                            // machine is added/deleted from the system.
                            try {
                                var machinesList = zooKeeper.getChildren(ZK_MACHINES_PATH, true);
                                // check for new machines
                                for (String machine: machinesList) {
                                    if (!machines.containsKey(machine)) {
                                        // new machine add to llist
                                        addMachine(machine);
                                        //var bytes = zooKeeper.getData(ZK_RESOURCES_PATH+machine, null, null);
                                        //MachineAD machineAD = SerializationUtils.deserialize(bytes);
                                        //
                                    }
                                }
                                // check for deleted machines and remove them
                                machines.forEach((machine, spec)->{
                                    if (!machinesList.contains(machine)){
                                        // remove
                                        machines.remove(machine);
                                        System.out.println("Machine is removed from system");
                                    }
                                });

                            } catch (KeeperException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            };

            this.tasksWatcher = new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {

                    // either node changed or removed
                    switch (watchedEvent.getType()) {
                        case NodeChildrenChanged ->  {
                            // new task is added to system or deleted. assign it to any resource
                            try {
                                var children = zooKeeper.getChildren(ZK_TASKS_PATH,tasksWatcher);
                                // sort reverse
                                children.sort(Comparator.reverseOrder());
                                // check for new tasks
                                children.forEach((task)-> {
                                    if (!matching.containsKey(task)) {
                                        addTask(task);
                                    }
                                    else {
                                        // this task is already assigned to a machine
                                        // check the status and free the resource.
                                    }
                                });

                                // delete for new tasks.
                                matching.forEach((machine,spec)->{
                                    if (!children.contains(machine)) {
                                        // task is deleted
                                        matching.remove(machine);
                                    }
                                });
                            } catch (KeeperException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }
            };

            this.zooKeeper = new ZooKeeper(zkConnectString, Constants.ZK_TIMEOUT, tasksWatcher);


            // create master node

            this.zooKeeper.create("/master",new byte[0], OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            System.out.println("Master created at zookeeper");

            this.zooKeeper.addWatch(ZK_TASKS_PATH,tasksWatcher, AddWatchMode.PERSISTENT); //TODO add watches for tasks and machines
            this.zooKeeper.addWatch(ZK_MACHINES_PATH, machineWatcher, AddWatchMode.PERSISTENT);
            System.out.println("Watches established");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    void start() {
        System.out.println("Starting the functionality");
        while(true) {

            Thread.onSpinWait();
        }
    }

    private void addMachine(String machineId) {
        try {
            var data = this.zooKeeper.getData(ZK_MACHINES_PATH +"/"+machineId, true, null);
            MachineAD machineAD = SerializationUtils.deserialize(data);
            // add task to matching and machines.
            machines.put(machineId,machineAD);

            System.out.println("Machine is added: "+machineId);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    private void addTask(String taskId) {

        // new task.

        try {
            var data = this.zooKeeper.getData("/tasks/"+taskId, true, null);
            JobAD task = SerializationUtils.deserialize(data);
            // add task to matching and tasks.

            tasks.put(taskId, task);

            System.out.println("Task is added: "+taskId);

            // create a matching
            var machineId = getResource(task);

            // put to database
            matching.put(taskId,machineId);
            // write to machine and tasks in zookeeper.
            var machineAddress =machines.get(machineId).address;
            var clientAddress = tasks.get(taskId).clientAddress;
            System.out.println("Task + "+taskId + " matched to "+ machineId);
            // write to zookeeper

            // need to write
            DTO.ZKObject object = new DTO.ZKObject(clientAddress,machineAddress, DTO.Status.ASSIGNED, taskId);
            var objectBytes = SerializationUtils.serialize(object);

            zooKeeper.setData(ZK_TASKS_PATH+"/"+taskId, objectBytes,-1);
            zooKeeper.setData(ZK_MACHINES_PATH +"/"+machineId, objectBytes,-1);
            System.out.println("Written to znodes");

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    private String getResource(JobAD ad) {

        var iterator = machines.keySet().iterator();

            if (iterator.hasNext())
                return iterator.next();

        return null;

    }

    public static void main(String[] args ) {

        Master master = new Master("localhost");
        master.start();

        //SecurityManager securityManager = System.getSecurityManager();
        //System.out.println(System.getProperty("java.version"));
    }
}
