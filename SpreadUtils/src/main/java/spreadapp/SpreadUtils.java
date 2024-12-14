package spreadapp;

import spread.SpreadConnection;
import spread.SpreadException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map;

// SpreadUtils: Utility class for Spread operations
public class SpreadUtils {
    private SpreadConnection connection;
    private final String groupName;
    private final GroupMember groupMember = new GroupMember();

    public SpreadUtils(String groupName) {
        this.groupName = groupName;
    }

    // Starts the Spread connection
    public void start(String address, int port, String name) {
        try {
            connection = new SpreadConnection();
            connection.connect(InetAddress.getByName(address), port, name, false, true);
        } catch (SpreadException e) {
            System.err.println("There was an error connecting to the daemon.");
            System.err.println(Arrays.toString(e.getStackTrace()));
            System.exit(1);
        } catch (UnknownHostException e) {
            System.err.println("Can't find the daemon " + address);
            System.exit(1);
        }
    }

    // Joins the group
    public void joinGroup() throws SpreadException {
        groupMember.joinGroup(connection, groupName);
    }

    // Leaves the group
    public void leaveGroup() throws SpreadException {
        groupMember.leaveGroup(groupName);
    }

    // Sends a message to the group
    public void sendMessage(String message) {
        try {
            groupMember.sendMessage(groupName, message, connection);
        } catch (SpreadException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    // Closes the connection
    public void close() throws SpreadException {
        connection.disconnect();
    }

    // Elects a leader and sends the leader info to the group
    public void electLeader(Map<String, Integer> loads, String leader) {
        try {
            groupMember.electLeader(groupName, loads, leader, connection);
        } catch (SpreadException e) {
            System.err.println("Error electing leader: " + e.getMessage());
        }
    }

    public SpreadConnection getConnection() {
        return connection;
    }
}