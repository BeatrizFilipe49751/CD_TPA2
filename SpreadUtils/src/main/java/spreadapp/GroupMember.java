package spreadapp;

import com.google.gson.Gson;
import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;
import java.util.HashMap;
import java.util.Map;

public class GroupMember {
    private final Map<String, SpreadGroup> groupsBelonging = new HashMap<>();

    public void leaveGroup(String groupName) throws SpreadException {
        SpreadGroup group = groupsBelonging.get(groupName);
        if (group != null) {
            group.leave();
            groupsBelonging.remove(groupName);
            System.out.println("Left from " + group + ".");
        } else {
            System.out.println("No group to leave.");
        }
    }

    public void joinGroup(SpreadConnection connection, String groupName) throws SpreadException {
        SpreadGroup newGroup = new SpreadGroup();
        newGroup.join(connection, groupName);
        groupsBelonging.put(groupName, newGroup);
    }

    public void sendMessage(String groupName, String message, SpreadConnection connection) throws SpreadException {
        SpreadMessage msg = new SpreadMessage();
        msg.setSafe();
        msg.addGroup(groupName);
        msg.setData(message.getBytes());
        connection.multicast(msg);
    }

    public void electLeader(String groupName, Map<String, Integer> loads, String leader, SpreadConnection connection) throws SpreadException {
        // Send the leader information to all members
        Map<String, Object> payload = new HashMap<>();
        payload.put("leader", leader);
        payload.put("loads", loads);

        // Convert the payload to JSON
        Gson gson = new Gson();
        String json = gson.toJson(payload);

        // Send the message to the group
        sendMessage(groupName, json, connection);
    }
}
