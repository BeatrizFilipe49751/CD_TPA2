package spreadapp;

import spread.AdvancedMessageListener;
import spread.MembershipInfo;
import spread.SpreadMessage;
import java.util.Map;

public class AdvancedMessageHandlingLeader implements AdvancedMessageListener {
    private final Map<String, Integer> loads;
    SpreadUtils spreadUtils;
    String name;
    public AdvancedMessageHandlingLeader(SpreadUtils spreadUtils, String name, Map<String, Integer> loads) {
        this.spreadUtils = spreadUtils;
        this.name = name;
        this.loads = loads;
    }

    @Override
    public void regularMessageReceived(SpreadMessage spreadMessage) {
        String message = new String(spreadMessage.getData());

        switch (message) {
            case "REMOVE_LOAD":
                String memberName = spreadMessage.getSender().toString().split("#")[1];
                loads.put(memberName, loads.getOrDefault(memberName, 0) - 1);
                System.out.println("Received REMOVE_LOAD from: " + memberName);
                checkIfLeaderElectionNeeded(memberName);
                break;
            case "GET_NAME:":
                String leastLoaded = getFirstLeastLoaded();
                spreadUtils.sendMessage("NAME:" + leastLoaded);
                loads.put(leastLoaded, loads.getOrDefault(leastLoaded, 0) + 1);
                checkIfLeaderElectionNeeded(leastLoaded);
                System.out.println("Sent NAME: " + leastLoaded);
                break;
            default:
                break;
        }
    }

    @Override
    public void membershipMessageReceived(SpreadMessage spreadMessage) {
        // When new member joins, send loads to the new member
        MembershipInfo info = spreadMessage.getMembershipInfo();
        if (info.isCausedByJoin()) {
            String newMember = info.getJoined().toString().split("#")[1];
            System.out.println("New member joined: " + newMember);
            loads.put(newMember, 0);
            checkIfLeaderElectionNeeded(newMember);
        } else if (info.isSelfLeave()) {
            String newLeader = loads.entrySet().stream()
                    .min(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
            loads.remove(name);
            spreadUtils.electLeader(loads, newLeader);
            spreadUtils.getConnection().remove(this);
        } else if (info.isCausedByLeave() || info.isCausedByDisconnect()) {
            // When a member leaves, remove the loads of the member
            String member = info.getLeft().toString().split("#")[1];
            System.out.println("Member left: " + member);
            loads.remove(member);
        }
    }

    private void checkIfLeaderElectionNeeded(String memberName) {
        System.out.println("Checking if leader election needed: " + memberName + " " + loads.get(memberName) + " " + loads.get(name));
        if (loads.get(memberName) < loads.get(name)) {
            System.out.println("Leader election needed");
            spreadUtils.electLeader(loads, memberName);
            spreadUtils.getConnection().add(new BasicMessageHandling(name, spreadUtils));
            spreadUtils.getConnection().remove(this);
        }
    }

    private String getFirstLeastLoaded() {
        return loads.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
