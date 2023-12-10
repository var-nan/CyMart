import java.io.Serializable;

/**
 * @author nandhan, Created on 03/12/23
 */
public class JobAD implements Serializable {

    DTO.Architecture architecture;
    DTO.OS os;

    String ownwerName;
    double computingTime;
    double memory;
    boolean needCheckpoint;
}
