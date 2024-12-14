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


public class SvcServer {
    private static final String groupName = "Servers"; // TODO - To command line args
    private static SpreadUtils spreadUtils;

    public static void main(String[] args) throws Exception { // args: registerIp registerPort port
        //String registerIp = args[0];  // IP of Register Server
        //int registerPort = Integer.parseInt(args[1]); // Port of Register Server
        int port = args.length < 3 ? 8500 : Integer.parseInt(args[2]) ; // Port for this server

        String name = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        spreadUtils = new SpreadUtils(groupName);
        spreadUtils.start("127.0.0.1", 4803, name);
        spreadUtils.getConnection().add(new BasicMessageHandling(name, spreadUtils));
        spreadUtils.joinGroup();

        // Send Endpoint to Register Server
        String message = "SvcServer endpoint: " + getExternalIp() + ":" + port;
        spreadUtils.sendMessage(message);

        Server server = ServerBuilder
                .forPort(port)
                .addService(new CtSvcServiceImpl(spreadUtils))
                .build();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                spreadUtils.leaveGroup(); // Leave the Spread group before the application terminates
                //spreadUtils.getConnection().remove();
                spreadUtils.close(); // Close the Spread connection
                System.out.println("Left the Spread group: " + groupName);
            } catch (Exception e) {
                System.err.println("Error while leaving the Spread group: " + e.getMessage());
            }
        }));

        System.out.println("SvcServer running on port " + port);
        server.start();
        server.awaitTermination();
    }

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