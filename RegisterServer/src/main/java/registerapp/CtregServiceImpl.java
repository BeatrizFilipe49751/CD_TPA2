package registerapp;

import ctregstubs.*;
import java.util.Map;

public class CtregServiceImpl extends CtregServiceGrpc.CtregServiceImplBase {

    private static Map<ServerEndPoint, Integer> serverLoads;

    public CtregServiceImpl(Map<ServerEndPoint, Integer> serverLoads) {
        CtregServiceImpl.serverLoads = serverLoads;
    }

    @Override
    public void getIp(Empty request, io.grpc.stub.StreamObserver<ServerEndPoint> responseObserver) {
        ServerEndPoint leastLoaded = serverLoads.entrySet()
                .stream()
                .min(Map.Entry.comparingByValue())
                .orElseThrow(() -> new IllegalStateException("No servers available"))
                .getKey();

        responseObserver.onNext(leastLoaded);
        responseObserver.onCompleted();
    }

    public static void updateServerLoad(ServerEndPoint endpoint, int load) {
        if(serverLoads.containsKey(endpoint)) // if the endpoint is already in the map
            serverLoads.replace(endpoint, load); // adds the new load value
        else
            serverLoads.put(endpoint, load); // puts the new load value
    }


}