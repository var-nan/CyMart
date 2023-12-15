package classes;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static classes.Constants.*;


/**
 * @author nandhan, Created on 26/11/23
 */
public class FileTransfer {


    /**
     * Performs file transfer from client to the machine at the provided address.
     * Server only accepts valid clients to connect.
     *
     * Blocks until a machine is connected.
     *
     * filename: can be relative.
     * @param machineAddress
     */
    static void sendScript(String machineAddress) {
        //byte[] buffer = new byte[SOCKET_BUFFER_SIZE];

        System.out.println("Starting server");
        try (ServerSocket server = new ServerSocket(Constants.CLIENT_PORT)) {

            System.out.println("Server Started");
            //Socket client;

            /*
            do {

                // TODO: create new security manager and restrict to only valid addresses.
                client = server.accept();

                if (client.getInetAddress().getHostAddress().equals(address)) {
                    // break
                    System.out.println("classes.Client connected");
                    break;
                } else {
                    // another client connected, close it
                    System.out.println("Unregistered client connected.");
                    try {
                        client.close();
                    } catch (IOException e) {
                        if (!client.isClosed())
                            client.close();
                    }
                }
            } while(true);

             */

            Socket client = getValidConnection(server,machineAddress);

            try (FileInputStream fileInputStream = new FileInputStream("./src/main/java/UITest/Computation.java");
                 BufferedInputStream inputStream = new BufferedInputStream(fileInputStream, SOCKET_BUFFER_SIZE);
                 BufferedOutputStream outputStream = new BufferedOutputStream(client.getOutputStream(),
                         SOCKET_BUFFER_SIZE);
            ) {
                System.out.println("Starting file transfer");

                int nBytes;
                int count = 0;

                /*
                while((nBytes = fileInputStream.read(buffer)) != -1) {
                    count += nBytes;
                    outputStream.write(buffer, 0,nBytes);
                }
                outputStream.flush(); // flush unwritten data.

                 */
                count = transfer(inputStream,outputStream);
                System.out.println("Script transfer complete. "+count + " bytes sent.");

            } catch (IOException e) {
                // do something
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Socket getValidConnection(ServerSocket server, String machineAddress) throws IOException {

        Socket client;
        do {

            // TODO: create new security manager and restrict to only valid addresses.
            client = server.accept();

            if (client.getInetAddress().getHostAddress().equals(machineAddress)) {
                // break
                System.out.println("classes.Client connected");
                break;
            } else {
                // another client connected, close it
                System.out.println("Unregistered client connected.");
                try {
                    client.close();
                } catch (IOException e) {
                    if (!client.isClosed())
                        client.close();
                }
            }
        } while(true);
        return client;
    }

    /**
     * Receives script by connecting to server at the given address.
     * @param address
     */
    static void receiveScript(String address) {

        String fileName = "src/main/java/UITest/Compute.java";

        try (Socket socket = new Socket(address, Constants.CLIENT_PORT);

             FileOutputStream fileOutputStream = new FileOutputStream(fileName);
             BufferedOutputStream outputStream =
                     new BufferedOutputStream(fileOutputStream,SOCKET_BUFFER_SIZE);

             BufferedInputStream inputStream =
                     new BufferedInputStream(socket.getInputStream(), SOCKET_BUFFER_SIZE);

        ) {
            System.out.println("Connected to server.");
            //byte[] buffer = new byte[SOCKET_BUFFER_SIZE];

            int count = transfer(inputStream,outputStream);

            System.out.println("Script received. "+count+ " bytes received");


            // change the class name in the file.
            Path path = Paths.get(fileName); // TODO: change value.
            //Charset charset = StandardCharsets.UTF_8;
            String content = Files.readString(path);
            content = content.replaceAll("Computation", "Compute");
            Files.writeString(path, content);
            System.out.println("File modification is complete.");


        } catch (UnknownHostException e) {
            System.out.println("Unknown host.");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Sends the script results from machine to client.
     *
     * machine reads the file from the input filename, and sends it to the
     * given address on client port.
     * @param clientAddress
     */
    public static void sendResult(String clientAddress) {

        try (Socket socket = new Socket(clientAddress, CLIENT_PORT);
             FileInputStream fileInputStream = new FileInputStream(OUTPUT_FILE);
             BufferedInputStream inputStream = new BufferedInputStream(fileInputStream, SOCKET_BUFFER_SIZE);
             BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream(), SOCKET_BUFFER_SIZE);

        ) {

            int count = 0;
            count = transfer(inputStream,outputStream);
            System.out.println("Sent Results to client. "+count+" bytes.");

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * classes.Client starts a server and receives computation output as a file.
     *
     * Clinet should call this method to get the result.
     * @param filename
     * @param address
     */
    public static void receiveResult( String filename, String address) {

        try (ServerSocket serverSocket = new ServerSocket(CLIENT_PORT)) {

            Socket socket = getValidConnection(serverSocket, address);

            try (FileOutputStream fileOutputStream = new FileOutputStream(filename);
                 BufferedOutputStream outputStream = new BufferedOutputStream(fileOutputStream);

                 BufferedInputStream inputStream = new BufferedInputStream(socket.getInputStream());
            ) {

                int count = transfer(inputStream, outputStream);
                System.out.println("Received Output file. "+count + " bytes.");
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static int transfer(BufferedInputStream inputStream,
                                BufferedOutputStream outputStream) throws IOException {

        //inputStream.read();

        byte[] buffer = new byte[SOCKET_BUFFER_SIZE];

        int nBytes, count = 0;
        while((nBytes = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, nBytes);
            count += nBytes;
        }
        outputStream.flush();

        return count;
    }

    public static void deleteFiles() {
        // deletes both the files from machine.
        // TODO
    }

    public static void main(String[] args) {

    }


}
