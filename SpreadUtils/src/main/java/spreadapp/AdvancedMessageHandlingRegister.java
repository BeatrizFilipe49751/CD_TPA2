package spreadapp;

import spread.AdvancedMessageListener;
import spread.MembershipInfo;
import spread.SpreadMessage;
import java.util.Map;

public class AdvancedMessageHandlingRegister implements AdvancedMessageListener {
    private final Map<String, ServerEndpoint> servers;
    SpreadUtils spreadUtils;
    public AdvancedMessageHandlingRegister(Map<String, ServerEndpoint> servers, SpreadUtils spreadUtils) {
        this.servers = servers;
        this.spreadUtils = spreadUtils;
    }

    @Override
    public void regularMessageReceived(SpreadMessage spreadMessage) {
        String message = new String(spreadMessage.getData());

        // Check if the message contains endpoint info
        if (message.startsWith("SvcServer endpoint:")) {
            // Parse the endpoint information (e.g., "127.0.0.1:8500")
            String endpointInfo = message.substring("SvcServer endpoint: ".length());

            //Divide endpointInfo into ip and port
            String[] parts = endpointInfo.split(":");
            String ip = parts[0];
            int port = Integer.parseInt(parts[1]);

            // store the endpoint information in serverLoads map
            ServerEndpoint endpoint = new ServerEndpoint(ip, port);
            String memberName = (spreadMessage.getSender().toString()).split("#")[1];
            servers.put(memberName, endpoint);

            System.out.println("Stored endpoint: " + ip + ":" + port + " for " + memberName);
        }
    }

    @Override
    public void membershipMessageReceived(SpreadMessage spreadMessage) {
        MembershipInfo info = spreadMessage.getMembershipInfo();
        if (info.isSelfLeave()) {
            System.out.println("I left the group");
            servers.clear();
        } else if (info.isCausedByLeave() || info.isCausedByDisconnect()) {
            String left = info.getLeft().toString().split("#")[1];
            System.out.println("Member left or disconnected: " + left);
            servers.remove(left);
        } else if (info.isCausedByJoin()) {
            String joined = info.getJoined().toString().split("#")[1];
            System.out.println("Member joined: " + joined);
            if (servers.isEmpty()) {
                spreadUtils.sendMessage("LEADER1");
            }
        }
    }

}
