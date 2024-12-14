package registerapp;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import spreadapp.AdvancedMessageHandlingRegister;
import spreadapp.ServerEndpoint;
import spreadapp.SpreadUtils;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RegisterServer {
    private static final String groupName = "Servers"; // TODO - To command line args
    private static final int svcPort = 8500;
    private static final Map<String, ServerEndpoint> servers = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        // Connect to Spread Group
        SpreadUtils spreadUtils = new SpreadUtils(groupName);
        spreadUtils.start("127.0.0.1", 4803, null);
        spreadUtils.joinGroup();

        AdvancedMessageHandlingRegister advancedMessageHandlingRegister = new AdvancedMessageHandlingRegister(servers, spreadUtils);
        spreadUtils.getConnection().add(advancedMessageHandlingRegister);

        Server server = ServerBuilder
                .forPort(svcPort)
                .addService(new CtregServiceImpl(servers, spreadUtils))
                .build();

        System.out.println("Register Server is running on port " + svcPort);
        server.start();

        // Add shutdown hook to ensure close() is called when the server is stopped
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                spreadUtils.leaveGroup(); // Leave the Spread group before the application terminates
                spreadUtils.getConnection().remove(advancedMessageHandlingRegister); // Remove the listener
                spreadUtils.close(); // Close the Spread connection
                System.out.println("Left the Spread group: " + groupName);
            } catch (Exception e) {
                System.err.println("Error closing Spread connection: " + e.getMessage());
            }
        }));

        server.awaitTermination();
    }

}