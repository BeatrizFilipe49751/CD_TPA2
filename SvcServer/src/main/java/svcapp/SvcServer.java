package svcapp;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import spread.SpreadConnection;
import spread.SpreadGroup;

import java.net.InetAddress;


public class SvcServer {

    private static SpreadConnection connection;
    private static final String groupName = "Servers";

    public static void main(String[] args) throws Exception { // args: registerIp registerPort port
        //String registerIp = args[0];  // IP of Register Server
        //int registerPort = Integer.parseInt(args[1]); // Port of Register Server
        int port = args.length < 3 ? 8500 : Integer.parseInt(args[2]) ; // Port for this server
/*
        // Connect to Spread Group
        connection = new SpreadConnection();
        connection.connect(InetAddress.getByName("127.0.0.1"), 4803, null, false, true);
        SpreadGroup group = new SpreadGroup();
        group.join(connection, groupName);
        System.out.println("Joined Spread Group: " + groupName);
*/
        // TODO check if everything is ok

        Server server = ServerBuilder
                .forPort(port)
                .addService(new CtSvcServiceImpl())
                .build();

        System.out.println("SvcServer running on port " + port);
        server.start();
        server.awaitTermination();
    }
}