import classes.Constants;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import static classes.Constants.CLIENT_PORT;
import static classes.Constants.SOCKET_BUFFER_SIZE;


/**
 * @author nandhan, Created on 26/11/23
 */
public class FileTransfer {


    /**
     * Performs file transfer to the client at the provided address.
     * Server only accepts valid clients to connect.
     *
     * Blocks until a client is connected.
     *
     * filename: can be relative.
     * @param fileName
     * @param address
     */
    static void sendTask(String fileName, String address) {
        byte[] buffer = new byte[SOCKET_BUFFER_SIZE];


        try (ServerSocket server = new ServerSocket(Constants.CLIENT_PORT)) {

            System.out.println("Server Started");
            Socket client;

            do {

                // TODO: create new security manager and restrict to only valid addresses.
                client = server.accept();

                if (client.getInetAddress().getHostAddress().equals(address)) {
                    // break
                    System.out.println("Client connected");
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


            try (FileInputStream fileInputStream = new FileInputStream(fileName);
                 OutputStream outputStream = new BufferedOutputStream(client.getOutputStream(),
                         SOCKET_BUFFER_SIZE);
            ) {
                System.out.println("Starting file transfer");

                int nBytes;
                int count = 0;

                while((nBytes = fileInputStream.read(buffer)) != -1) {
                    count += nBytes;
                    outputStream.write(buffer, 0,nBytes);
                }
                outputStream.flush(); // flush unwritten data.

                System.out.println("File transfer complete. "+count + " bytes sent.");

            } catch (IOException e) {
                // do something
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void receiveScript(String fileName, String address) {

        try (Socket socket = new Socket(address, Constants.CLIENT_PORT);
             BufferedInputStream inputStream =
                     new BufferedInputStream(socket.getInputStream(), SOCKET_BUFFER_SIZE);
             FileOutputStream fileOutputStream = new FileOutputStream(fileName);
             BufferedOutputStream outputStream =
                     new BufferedOutputStream(fileOutputStream,SOCKET_BUFFER_SIZE);

        ) {
            System.out.println("Connected to server.");
            byte[] buffer = new byte[SOCKET_BUFFER_SIZE];

            int nBytes, count = 0;

            while ((nBytes = inputStream.read(buffer)) != -1) {

                outputStream.write(buffer, 0, nBytes);
                count += nBytes;
            }
            outputStream.flush(); // flush for last bytes.

            System.out.println("File received. "+count+ " bytes received");

        } catch (UnknownHostException e) {
            System.out.println("Unknown host.");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendResult(String fileName, String address) {

        // client should call this function.

        try (Socket socket = new Socket(address, CLIENT_PORT);
             FileInputStream fileInputStream = new FileInputStream(fileName);
             BufferedInputStream inputStream = new BufferedInputStream(fileInputStream, SOCKET_BUFFER_SIZE);
             BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream(), SOCKET_BUFFER_SIZE);

        ) {

            byte[] buffer = new byte[SOCKET_BUFFER_SIZE];

            int nBytes, count = 0;
            while((nBytes = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, nBytes);
                count += nBytes;
            }
            outputStream.flush();
            System.out.println("Sent Results to client. "+count+" bytes.");

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

    }


}
