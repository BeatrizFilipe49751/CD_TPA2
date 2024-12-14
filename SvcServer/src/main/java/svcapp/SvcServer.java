package svcapp;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import spreadapp.BasicMessageHandling;
import spreadapp.SpreadUtils;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

// SvcServer class handles the server setup and communicates with the Spread group and RabbitMQ
public class SvcServer {
    private static String groupName;
    private static SpreadUtils spreadUtils;

    public static void main(String[] args) throws Exception {
        if (args.length < 7) {
            System.err.println("Usage: java -jar SvcServer-1.0-jar-with-dependencies.jar <port> <SpreadGroup> <SpreadAddress> <SpreadPort> <ipRabbitMQ> <portRabbitMQ> <workQueue>");
            System.exit(1);
        }
        // Parse arguments to get configuration details
        int port = Integer.parseInt(args[0]);
        groupName = args[1];
        String spreadAddress = args[2];
        int spreadPort = Integer.parseInt(args[3]);
        String ipRabbitMQ = args[4];
        int portRabbitMQ = Integer.parseInt(args[5]);
        String workQueue = args[6];

        // Generate a unique name for the server instance
        String name = UUID.randomUUID().toString().replace("-", "").substring(0, 6);

        // Initialize SpreadUtils and start connection to Spread group
        spreadUtils = new SpreadUtils(groupName);
        spreadUtils.start(spreadAddress, spreadPort, name);
        spreadUtils.getConnection().add(new BasicMessageHandling(name, spreadUtils));
        spreadUtils.joinGroup();

        // Send Endpoint to Register Server
        String message = "SvcServer endpoint: " + getExternalIp() + ":" + port;
        spreadUtils.sendMessage(message);

        // Create the gRPC server and add the service implementation
        Server server = ServerBuilder
                .forPort(port)
                .addService(new CtSvcServiceImpl(spreadUtils, ipRabbitMQ, portRabbitMQ, workQueue))
                .build();

        // Add shutdown hook to leave the Spread group before exiting
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                spreadUtils.leaveGroup();
                spreadUtils.close();
                System.out.println("Left the Spread group: " + groupName);
            } catch (Exception e) {
                System.err.println("Error while leaving the Spread group: " + e.getMessage());
            }
        }));
        System.out.println("SvcServer running on port " + port);
        server.start();
        // Wait for server termination
        server.awaitTermination();
    }

    // Get the external IP address of the VM instance
    private static String getExternalIp() throws Exception {
        // URL for getting the external IP from the metadata server
        String metadataUrl = "http://169.254.169.254/computeMetadata/v1/instance/network-interfaces/0/access-configs/0/external-ip";

        HttpURLConnection con = (HttpURLConnection) new URL(metadataUrl).openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Metadata-Flavor", "Google"); // Required header for metadata server access

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                return in.readLine(); // Should return the external IP as a single line
            }
        } else {
            throw new Exception("Failed to get external IP: " + responseCode);
        }
    }

}