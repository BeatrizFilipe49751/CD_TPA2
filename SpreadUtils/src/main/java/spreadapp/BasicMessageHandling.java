package spreadapp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import spread.*;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BasicMessageHandling implements BasicMessageListener {
    String name;
    SpreadUtils spreadUtils;
    public BasicMessageHandling(String name, SpreadUtils spreadUtils) {
        this.name = name;
        this.spreadUtils = spreadUtils;
    }

    @Override
    public void messageReceived(SpreadMessage spreadMessage) {
        String message = new String(spreadMessage.getData());
        if (isJson(message)) {
            Gson gson = new Gson();
            Type payloadType = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> payload = gson.fromJson(message, payloadType);

            String leader = (String) payload.get("leader");
            Map<String, Integer> loads = gson.fromJson(gson.toJson(payload.get("loads")), new TypeToken<Map<String, Integer>>() {}.getType());

            System.out.println("Leader: " + leader);
            if (leader.equals(name)) {
                System.out.println("I am the leader");
                spreadUtils.getConnection().add(new AdvancedMessageHandlingLeader(spreadUtils, name, loads));
                spreadUtils.getConnection().remove(this);
            } else {
                System.out.println("I am not the leader");
            }
        } else if (message.equals("LEADER1")) {
            System.out.println("I am the leader");
            ConcurrentHashMap<String, Integer> loads = new ConcurrentHashMap<>();
            loads.put(name, 0);
            spreadUtils.getConnection().add(new AdvancedMessageHandlingLeader(spreadUtils, name, loads));
            spreadUtils.getConnection().remove(this);
        }
    }

    private boolean isJson(String payload) {
        payload = payload.trim();
        return (payload.startsWith("{") && payload.endsWith("}")) || (payload.startsWith("[") && payload.endsWith("]"));
    }
}