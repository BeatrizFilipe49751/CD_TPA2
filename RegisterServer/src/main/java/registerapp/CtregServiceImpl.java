package registerapp;

import ctregstubs.*;
import io.grpc.stub.StreamObserver;
import spread.BasicMessageListener;
import spread.SpreadMessage;
import spreadapp.ServerEndpoint;
import spreadapp.SpreadUtils;
import java.util.Map;

public class CtregServiceImpl extends CtregServiceGrpc.CtregServiceImplBase {
    // Map holding the registered servers endpoints
    private static Map<String, ServerEndpoint> servers;
    private final SpreadUtils spreadUtils;

    public CtregServiceImpl(Map<String, ServerEndpoint> servers, SpreadUtils spreadUtils) {
        CtregServiceImpl.servers = servers;
        this.spreadUtils = spreadUtils;
    }

    // gRPC method to handle the "getIp" request from clients
    @Override
    public void getIp(Empty request, StreamObserver<ServerEndPoint> responseObserver) {
        // Send a message to request the least loaded server name
        String message = "GET_NAME:";
        spreadUtils.sendMessage(message);

        // Add a listener to the connection to receive the response from leader
        spreadUtils.getConnection().add(new BasicMessageListener() {
            @Override
            public void messageReceived(SpreadMessage spreadMessage) {
                String receivedMessage = new String(spreadMessage.getData());
                // If the received message contains the server name, retrieve its endpoint
                if (receivedMessage.startsWith("NAME:")) {
                    String[] parts = receivedMessage.split(":");
                    String name = parts[1];
                    ServerEndpoint server = servers.get(name);
                    // Create a response with the server's IP and port
                    ServerEndPoint response = ServerEndPoint.newBuilder()
                            .setIp(server.getIp())
                            .setPort(server.getPort())
                            .build();
                    // Send the response back to the client
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                    spreadUtils.getConnection().remove(this);
                }
            }
        });
    }

}