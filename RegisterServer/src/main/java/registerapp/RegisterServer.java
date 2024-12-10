package registerapp;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import ctregstubs.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RegisterServer {
    /*private static final int svcPort = 8500;
    private static final Map<ServerEndPoint, Integer> serverLoads = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        Server server = ServerBuilder
                .forPort(svcPort)
                .addService(new CtregServiceImpl(serverLoads))
                .build();

        System.out.println("Register Server is running on port " + svcPort);
        server.start();
        server.awaitTermination();
    }*/

}