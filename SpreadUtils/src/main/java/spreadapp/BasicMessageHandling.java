package spreadapp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import spread.*;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// BasicMessageHandling: Message handling for the non-leader nodes
public class BasicMessageHandling implements BasicMessageListener {
    String name; // Name of the server
    SpreadUtils spreadUtils;
    public BasicMessageHandling(String name, SpreadUtils spreadUtils) {
        this.name = name;
        this.spreadUtils = spreadUtils;
    }

    @Override
    public void messageReceived(SpreadMessage spreadMessage) {
        String message = new String(spreadMessage.getData());
        if (isJson(message)) {
            // Process JSON messages for leader election and load distribution
            Gson gson = new Gson();
            Type payloadType = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> payload = gson.fromJson(message, payloadType);

            String leader = (String) payload.get("leader");
            Map<String, Integer> loads = gson.fromJson(gson.toJson(payload.get("loads")), new TypeToken<Map<String, Integer>>() {}.getType());
            if (leader.equals(name)) {
                // This server becomes the leader
                System.out.println("I am the leader");
                spreadUtils.getConnection().add(new AdvancedMessageHandlingLeader(spreadUtils, name, loads));
                spreadUtils.getConnection().remove(this);
            } else {
                System.out.println("I am not the leader");
            }
        } else if (message.equals("LEADER1")) {
            // This server becomes the leader
            System.out.println("I am the leader");
            ConcurrentHashMap<String, Integer> loads = new ConcurrentHashMap<>();
            loads.put(name, 0);
            spreadUtils.getConnection().add(new AdvancedMessageHandlingLeader(spreadUtils, name, loads));
            spreadUtils.getConnection().remove(this);
        }
    }

    // Utility to check if a message is in JSON format
    private boolean isJson(String payload) {
        payload = payload.trim();
        return (payload.startsWith("{") && payload.endsWith("}")) || (payload.startsWith("[") && payload.endsWith("]"));
    }
}