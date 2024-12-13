package spreadapp;

import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SpreadCommonMsg {

    private final boolean isAdvanced;
    private SpreadConnection connection;
    private final Map<String, SpreadGroup> groupsBelonging = new HashMap<>();

    // If isAdvanced is true, then advancedMsgHandling is used; otherwise msgHandler is used.
    private MessageHandling msgHandler = null;
    private AdvancedMessageHandling advancedMsgHandling = null;

    public SpreadCommonMsg(Boolean isAdvanced) {
        this.isAdvanced = isAdvanced;
    }

    public void start(String user, String address, int port) {
        // Establish the spread connection.
        try {
            connection = new SpreadConnection();
            connection.connect(InetAddress.getByName(address), port, user, false, true);

            if(isAdvanced) {
                advancedMsgHandling = new AdvancedMessageHandling(connection);
                connection.add(advancedMsgHandling);
            } else {
                msgHandler = new MessageHandling(connection);
                connection.add(msgHandler);
            }
        } catch (SpreadException e) {
            System.err.println("There was an error connecting to the daemon.");
            //e.printStackTrace();
            System.err.println(Arrays.toString(e.getStackTrace()));
            System.exit(1);
        } catch (UnknownHostException e) {
            System.err.println("Can't find the daemon " + address);
            System.exit(1);
        }
    }

    public void joinGroup(String groupName) throws SpreadException {
        SpreadGroup newGroup = new SpreadGroup();
        newGroup.join(connection, groupName);
        groupsBelonging.put(groupName, newGroup);
    }

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

    public void sendMessage(String groupName, String message) throws SpreadException {
        SpreadMessage msg = new SpreadMessage();
        msg.setSafe();
        msg.addGroup(groupName);
        msg.setData(message.getBytes());
        connection.multicast(msg);
    }

    public void close() throws SpreadException {
        // remove listener
        if(isAdvanced) {
            connection.remove(advancedMsgHandling);
        } else {
            connection.remove(msgHandler);
        }
        // connection.remove(advancedMsgHandling);
        connection.disconnect();
    }

}
