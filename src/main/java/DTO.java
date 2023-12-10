import java.io.Serializable;

/**
 * @author nandhan, Created on 03/12/23
 */
public class DTO {
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
