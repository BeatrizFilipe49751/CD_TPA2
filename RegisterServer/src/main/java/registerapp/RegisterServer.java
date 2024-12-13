package registerapp;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import ctregstubs.*;
import spread.SpreadConnection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RegisterServer extends CtregServiceGrpc.CtregServiceImplBase {

    private static SpreadConnection connection;
    private static final String groupName = "Servers";
    private static final int svcPort = 8500;
    private static final Map<ServerEndPoint, Integer> serverLoads = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        /*
        // Connect to Spread Group
        connection = new SpreadConnection();
        connection.connect(InetAddress.getByName("127.0.0.1"), 4803, null, false, true);
        SpreadGroup group = new SpreadGroup();
        group.join(connection, groupName);
        System.out.println("Joined Spread Group: " + groupName);
        */

        //TODO leadership election

        Server server = ServerBuilder
                .forPort(svcPort)
                .addService(new CtregServiceImpl(serverLoads))
                .build();

        System.out.println("Register Server is running on port " + svcPort);
        server.start();
        server.awaitTermination();
    }


}