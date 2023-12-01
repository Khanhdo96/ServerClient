import java.io.*;
import java.net.*;
import java.util.Scanner;


class client {


    public static void main(String args[]) {
        try {
            // Connect to the server at localhost (127.0.0.1) on port 6666
            Socket mySocket = new Socket("127.0.0.1", 6666);
            // Set up output stream to send data to the server
            DataOutputStream outStream = new DataOutputStream(mySocket.getOutputStream());
            // Set up input stream to receive data from the server
            BufferedReader inStream = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));

            // Scanner for reading user input from the console
            Scanner userInput = new Scanner(System.in);


            String statement = "";
            // Loop until the user inputs "Exit"
            while (!statement.equalsIgnoreCase("Exit")) {
                System.out.print("Enter command: ");
                // Read user command from the console
                statement = userInput.nextLine();

                // Send the user command to the server
                outStream.writeBytes(statement + "\n");
                outStream.flush(); // Ensure the command is sent immediately

                // Read the server's response
                String serverResponse = inStream.readLine();
                if (serverResponse != null) {
                    System.out.println("Server response: " + serverResponse);
                }
            }

            // Close all streams and the socket when done
            System.out.println("Closing the connection and the sockets");
            outStream.close();
            inStream.close();
            mySocket.close();


        } catch (Exception exc) {
            // Handle exceptions
            System.out.println("Error: " + exc.toString());
        }
    }
}
