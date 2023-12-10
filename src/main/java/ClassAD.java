import java.io.Serializable;

/**
 * @author nandhan, Created on 26/11/23
 */
public class ClassAD implements Serializable {

    enum MachineType implements Serializable {
        RESOURCE,
        USER
    }

    MachineType type;

    String name;

    double computingTime;

    // TODO add more specs.

}
