import java.io.Serializable;


/**
 * @author nandhan, Created on 03/12/23
 */
public class ResourceAD implements Serializable {

    DTO.Architecture architecture;
    DTO.OS os;

    String ownerName;
    String machineName;
    double memory;
    double computingTime;

    ResourceAD() {

    }
}
