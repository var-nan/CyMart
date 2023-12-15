package classes;

import classes.DTO;

import java.io.Serializable;

/**
 * @author nandhan, Created on 03/12/23
 */
public class JobAD implements Serializable {

    DTO.Architecture architecture;
    DTO.OS os;

    String clientName;
    String clientAddress;

    double computingTime;
    double memoryNeeded;

    boolean needCheckpoint;

    JobAD() {

    }

    JobAD(String clientName, String clientAddress) {
        this.clientAddress = clientAddress;
        this.clientName = clientName;
        this.computingTime = 1.0;
        this.memoryNeeded = 2.0; // TODO change when implementing matchmaking.
        this.os = DTO.OS.LINUX;
        this.architecture = DTO.Architecture.INTEL;
        this.needCheckpoint = false;
    }
}
