import java.io.FilePermission;
import java.security.Policy;

/**
 * @author nandhan, Created on 09/12/23
 */
public class SandboxSM extends SecurityManager{

    FilePermission filePermission = new FilePermission("output.txt", "read,write");

    public static void main(String[] args) {

    }

}
