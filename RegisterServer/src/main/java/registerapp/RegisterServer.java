package registerapp;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import spreadapp.AdvancedMessageHandlingRegister;
import spreadapp.ServerEndpoint;
import spreadapp.SpreadUtils;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RegisterServer {
    private static String groupName;
    private static final int svcPort = 8500;
    private static final Map<String, ServerEndpoint> servers = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: java -jar RegisterServer-1.0-jar-with-dependencies.jar <SpreadGroup> <SpreadAddress> <SpreadPort>");
            System.exit(1);
        }
        groupName = args[0];
        String spreadAddress = args[1];
        int spreadPort = Integer.parseInt(args[2]);

        // Connect to Spread Group
        SpreadUtils spreadUtils = new SpreadUtils(groupName);
        spreadUtils.start(spreadAddress, spreadPort, null);
        spreadUtils.joinGroup();

        // Register a message handler for managing server registrations
        AdvancedMessageHandlingRegister advancedMessageHandlingRegister = new AdvancedMessageHandlingRegister(servers, spreadUtils);
        spreadUtils.getConnection().add(advancedMessageHandlingRegister);

        // Set up the gRPC server to handle requests on the specified port
        Server server = ServerBuilder
                .forPort(svcPort)
                .addService(new CtregServiceImpl(servers, spreadUtils))
                .build();

        // Start the server and display the status
        System.out.println("Register Server is running on port " + svcPort);
        server.start();

        // Add a shutdown hook to gracefully leave the Spread group and close connections
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                spreadUtils.leaveGroup();
                spreadUtils.getConnection().remove(advancedMessageHandlingRegister);
                spreadUtils.close();
                System.out.println("Left the Spread group: " + groupName);
            } catch (Exception e) {
                System.err.println("Error closing Spread connection: " + e.getMessage());
            }
        }));

        // Wait for the server to terminate
        server.awaitTermination();
    }

}