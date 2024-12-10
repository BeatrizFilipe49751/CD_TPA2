package svcapp;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import spread.SpreadConnection;

public class SvcServer {

    private static SpreadConnection connection;
    private static String groupName = "Servers";

    public static void main(String[] args) throws Exception {
        /*String registerIp = args[0];  // IP of Register Server
        int registerPort = Integer.parseInt(args[1]); // Port of Register Server
        int port = Integer.parseInt(args[2]); // Port for this server

        // Connect to Spread Group
        connection = new SpreadConnection();
        connection.connect(InetAddress.getByName("127.0.0.1"), 4803, null, false, true);
        SpreadGroup group = new SpreadGroup();
        group.join(connection, groupName);
        System.out.println("Joined Spread Group: " + groupName);*/

        int port = 8500;
        // Start gRPC Server
        Server server = ServerBuilder
                .forPort(port)
                .addService(new CtSvcServiceImpl())
                .build();

        System.out.println("SvcServer running on port " + port);
        server.start();
        server.awaitTermination();
    }
}