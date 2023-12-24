package classes;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * @author nandhan, Created on 03/12/23
 */
public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        //classes.FileTransfer fileTransfer = new classes.FileTransfer("sample.txt");
        //fileTransfer.startTransfer();

        var pb = new ProcessBuilder("java","src/main/java/UITest/Computation.java").start();

        pb.waitFor();

        try (BufferedInputStream inputStream = new BufferedInputStream(pb.getInputStream());
             BufferedInputStream errorStream = new BufferedInputStream(pb.getErrorStream());

             PrintStream printWriter = new PrintStream(System.out);
        ) {
            byte[] output = inputStream.readAllBytes();
            byte[] error = errorStream.readAllBytes();

            printWriter.println("Output: ");
            printWriter.write(output);
            printWriter.flush();

            printWriter.println("Error");
            printWriter.write(error);
            printWriter.flush();

        }
    }
}
