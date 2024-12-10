package spreadapp;

import spread.SpreadConnection;
import spread.SpreadGroup;
import spread.SpreadMessage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class SpreadUtils {

    // VM with spreaad
    //private static String daemonIP="35.195.114.14"; // Node1
    private static String daemonIP="34.140.140.168"; // Node2

    private static int daemonPort=4803;

    private SpreadConnection connection;

    private final Map<String, SpreadGroup> groupsBelonging=new HashMap<String,SpreadGroup>();

    private static String groupName = "Servers";

    public void joinGroup(String groupName) throws SpreadException {
        SpreadGroup newGroup=new SpreadGroup();
        newGroup.join(connection, groupName);
        groupsBelonging.put(groupName,newGroup);
    }

    public void leaveGroup(String groupName) throws SpreadException {
        SpreadGroup group=groupsBelonging.get(groupName);
        if(group != null) {
            group.leave();
            groupsBelonging.remove(groupName);
            System.out.println("Left from " + group + ".");
        } else  { System.out.println("No group to leave."); }
    }

    private AdvancedMessageHandling advancedMsgHandling;
    public void start(String user, String address, int port) {
        // Establish the spread connection.
        try  {
            connection = new SpreadConnection();
            connection.connect(InetAddress.getByName(address), port, user, false, true);

            advancedMsgHandling = new AdvancedMessageHandling(connection);
            connection.add(advancedMsgHandling);
        }
        catch(SpreadException e)  {
            System.err.println("There was an error connecting to the daemon.");
            e.printStackTrace();
            System.exit(1);
        }
        catch(UnknownHostException e) {
            System.err.println("Can't find the daemon " + address);
            System.exit(1);
        }
    }

    public void close() throws SpreadException {
        // remove listener
        connection.remove(advancedMsgHandling);
        //connection.remove(advancedMsgHandling);
        // Disconnect.
        connection.disconnect();
    }

    public void sendMessage(String groupName, String message) throws SpreadException {
        SpreadMessage msg = new SpreadMessage();
        msg.setSafe();
        msg.addGroup(groupName);
        msg.setData(message.getBytes());
        connection.multicast(msg);
    }

}



