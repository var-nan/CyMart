package classes;

import java.io.Serializable;

/**
 * @author nandhan, Created on 03/12/23
 */
public class DTO {

    enum Status implements Serializable {
        INIT, ASSIGNED, COMPLETED
    }

    static class ZKObject implements Serializable {

        String clientAddress;
        Status status;
        String machineAddress;
        String taskID; // task path in zookeeper.

        ZKObject() {

        }
        ZKObject(String clientAddress, String machineAddress, Status status, String taskID) {
            this.status = status;
            this.clientAddress = clientAddress;
            this.machineAddress = machineAddress;
            this.taskID =taskID;
        }

    }

    enum OS implements Serializable {
        WINDOWS,
        LINUX,
        MAC_OS,
        UNIX
    }

    enum Architecture implements Serializable {
        INTEL,
        ARM
    }
}
