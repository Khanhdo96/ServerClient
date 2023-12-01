import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
class server {


    // HashMap to store usernames and their corresponding message lists
    private static Map<String, List<String>> mailboxes = new HashMap<>();

    public static void main(String args[]) {
        try {
            // Create a server socket on port 6666
            ServerSocket mySocket = new ServerSocket(6666);
            System.out.println("Server started on port 6666 ....");

            // Continuously listen for client connections
            while (true) {
                Socket connectedClient = mySocket.accept();
                System.out.println("Connection established with a client");

                // Handle each client in a separate thread
                Thread clientThread = new Thread(() -> handleClient(connectedClient));
                clientThread.start();
            }


        } catch (Exception exc) {
            System.out.println("Error: " + exc.toString());
        }
    }

    // Method to handle a connected client
    private static void handleClient(Socket connectedClient) {
        try {
            // Set up input and output streams for the client
            BufferedReader br = new BufferedReader(new InputStreamReader(connectedClient.getInputStream()));
            PrintStream ps = new PrintStream(connectedClient.getOutputStream());

            // Read the client's initial command
            String inputCommand = br.readLine();
            String[] commandParts = inputCommand.split(" ", 2);

            // Process initial command
            if (commandParts.length == 2 && commandParts[0].equals("NewClient")) {
                // Register a new client
                String newUsername = commandParts[1];
                if (!mailboxes.containsKey(newUsername)) {
                    mailboxes.put(newUsername, new ArrayList<>());
                    ps.println("Welcome to our communication server, you are added as a new client");
                } else {
                    ps.println("This username already exists");
                }
            } else {
                ps.println("Invalid command format. Please use 'NewClient <username>' to register.");
            }

            // Process further commands from the client
            String inputData;
            while ((inputData = br.readLine()) != null && !inputData.equals("Exit")) {
                processCommand(commandParts[1], inputData, ps);
            }

            // Close connection after the client exits
            System.out.println("Closing the connection for " + commandParts[1]);
            br.close();
            ps.close();
            connectedClient.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to process different commands from a client
    private static void processCommand(String username, String command, PrintStream ps) {
        String[] parts = command.split(" ", 2);

        // Handle different types of commands: NewClient, Push, Pull, KnowOthers
        if (parts[0].equals("NewClient")) {
            String newUsername = parts[1];
            if (!mailboxes.containsKey(newUsername)) {
                mailboxes.put(newUsername, new ArrayList<>());
                ps.println("Welcome to our communication server, you are added as a new client");
            } else {
                ps.println("This username already exists");
            }
        } else if (parts[0].equals("Push")) {
            String[] pushParts = parts[1].split(", ", 2); // Split only into two parts
            if (pushParts.length == 2) {
                String sender = pushParts[0];
                String receiverAndMessage = pushParts[1];


                // Separate receivers and message content using the last occurrence of ','
                int lastCommaIndex = receiverAndMessage.lastIndexOf(", ");
                if (lastCommaIndex != -1) {
                    String receivers = receiverAndMessage.substring(0, lastCommaIndex);
                    String message = receiverAndMessage.substring(lastCommaIndex + 2);


                    if (mailboxes.containsKey(sender)) {
                        // Parse receivers properly
                        String[] receiverList = receivers.split(", ");
                        List<String> validReceivers = new ArrayList<>();


                        for (String receiver : receiverList) {
                            String cleanReceiver = receiver.trim();
                            cleanReceiver = cleanReceiver.replaceAll("[\\[\\](){}]", ""); // Remove any brackets or parentheses


                            if (cleanReceiver.equals("ALL")) {
                                mailboxes.keySet().stream()
                                        .filter(key -> !key.equals(sender))
                                        .forEach(key -> mailboxes.get(key).add("From " + sender + ": " + message));
                                validReceivers.add(cleanReceiver);
                            } else if (mailboxes.containsKey(cleanReceiver)) {
                                String formattedMessage = "From " + sender + ": " + message;
                                mailboxes.get(cleanReceiver).add(formattedMessage);
                                validReceivers.add(cleanReceiver);
                            } else {
                                ps.println("This receiver does not exist: " + cleanReceiver);
                            }
                        }


                        if (!validReceivers.isEmpty()) {
                            ps.println("The message was successfully forwarded to the receiver(s)");
                        }
                    } else {
                        ps.println("This username does not exist");
                    }
                } else {
                    ps.println("Invalid Push command format");
                }
            } else {
                ps.println("Invalid Push command format");
            }
        } else if (parts[0].equals("Pull")) {
            String recipient = parts[1].trim();
            String cleanRecipient = recipient.startsWith("NewClient ") ? recipient.substring(10) : recipient;


            if (mailboxes.containsKey(recipient)) {
                List<String> messages = mailboxes.get(recipient);
                if (!messages.isEmpty()) {
                    ps.println("Messages for " + cleanRecipient + ": " + messages);
                    // Don't clear messages here, let them persist until pulled by the recipient
                } else {
                    ps.println("No new messages for " + cleanRecipient);
                }
            } else {
                ps.println("This username does not exist");
            }
        } else if (parts[0].equals("KnowOthers")) {
            StringBuilder registeredClients = new StringBuilder("Registered clients: ");
            for (String key : mailboxes.keySet()) {
                if (key.startsWith("NewClient ")) {
                    registeredClients.append(key.substring(10)).append(", ");
                } else {
                    registeredClients.append(key).append(", ");
                }
            }
            // Remove the trailing ", " before printing
            if (registeredClients.length() > 16) {
                registeredClients.setLength(registeredClients.length() - 2);
            }
            ps.println(registeredClients.toString());
        } else {
            ps.println("Unknown command: " + command);
        }
    }
}
