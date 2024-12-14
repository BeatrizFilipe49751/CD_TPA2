package registerapp;

import ctregstubs.*;
import io.grpc.stub.StreamObserver;
import spread.BasicMessageListener;
import spread.SpreadMessage;
import spreadapp.ServerEndpoint;
import spreadapp.SpreadUtils;
import java.util.Map;

public class CtregServiceImpl extends CtregServiceGrpc.CtregServiceImplBase {
    private static Map<String, ServerEndpoint> servers;
    private final SpreadUtils spreadUtils;

    public CtregServiceImpl(Map<String, ServerEndpoint> servers, SpreadUtils spreadUtils) {
        CtregServiceImpl.servers = servers;
        this.spreadUtils = spreadUtils;
    }

    @Override
    public void getIp(Empty request, StreamObserver<ServerEndPoint> responseObserver) {
        String message = "GET_NAME:";
        spreadUtils.sendMessage(message);
        spreadUtils.getConnection().add(new BasicMessageListener() {
            @Override
            public void messageReceived(SpreadMessage spreadMessage) {
                String receivedMessage = new String(spreadMessage.getData());
                if (receivedMessage.startsWith("NAME:")) {
                    String[] parts = receivedMessage.split(":");
                    String name = parts[1];
                    ServerEndpoint server = servers.get(name);
                    ServerEndPoint response = ServerEndPoint.newBuilder()
                            .setIp(server.getIp())
                            .setPort(server.getPort())
                            .build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                    spreadUtils.getConnection().remove(this);
                }
            }
        });
    }

}