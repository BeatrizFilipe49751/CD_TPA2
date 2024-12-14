package spreadapp;

import spread.SpreadConnection;
import spread.SpreadException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map;

public class SpreadUtils {
    private SpreadConnection connection;
    private final String groupName;
    private final GroupMember groupMember = new GroupMember();

    public SpreadUtils(String groupName) {
        this.groupName = groupName;
    }

    public void start(String address, int port, String name) {
        // Establish the spread connection.
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

    public void joinGroup() throws SpreadException {
        groupMember.joinGroup(connection, groupName);
    }

    public void leaveGroup() throws SpreadException {
        groupMember.leaveGroup(groupName);
    }

    public void sendMessage(String message) {
        try {
            groupMember.sendMessage(groupName, message, connection);
        } catch (SpreadException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    public void close() throws SpreadException {
        // remove listener
        connection.disconnect();
    }

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