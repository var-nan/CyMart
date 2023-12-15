package classes;

import classes.DTO;

import java.io.Serializable;


/**
 * @author nandhan, Created on 03/12/23
 */
public class MachineAD implements Serializable {

    DTO.Architecture architecture;
    DTO.OS os;

    String address;
    String machineName;
    double memory;

    MachineAD() {

    }

    MachineAD(String address) {
        this.machineName = "Nvidia-Server";
        this.address = address;
        this.os = DTO.OS.LINUX;
        this.architecture = DTO.Architecture.INTEL;
        this.memory = 2.0;
    }
}
