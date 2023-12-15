package classes;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

import static classes.Constants.*;
import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;

/**
 * @author nandhan, Created on 10/12/23
 */
public class SetupScript {


    static void setUp() {
        // connect to zookeeper and setup nodes

        try {
            ZooKeeper zooKeeper = new ZooKeeper("localhost", ZK_TIMEOUT,null);

            // set up nodes

            // assign path
            /*
            if ((zooKeeper.exists(ZK_ASSIGN_PATH, null)) != null) {
                // node is already present, remove it and recreate it.
                zooKeeper.delete(ZK_ASSIGN_PATH, -1);
            }
            zooKeeper.create(ZK_ASSIGN_PATH, "".getBytes(),OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

             */
            // resources path
            if ((zooKeeper.exists(ZK_MACHINES_PATH, null)) != null) {
                zooKeeper.delete(ZK_MACHINES_PATH, -1);
            }
            zooKeeper.create(ZK_MACHINES_PATH, new byte[0], OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

            // tasks path
            if ((zooKeeper.exists(ZK_TASKS_PATH, null)) != null) {
                zooKeeper.delete(ZK_TASKS_PATH, -1);
            }
            // assign path
            zooKeeper.create(ZK_TASKS_PATH, new byte[0], OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    static void cleanup() {

        // TODO clean all persistent nodes.
    }

    public static void main(String[] args) {
        setUp();
    }

}
