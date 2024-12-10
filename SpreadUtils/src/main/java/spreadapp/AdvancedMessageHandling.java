package spreadapp;

import spread.*;

public class AdvancedMessageHandling implements AdvancedMessageListener {
    private SpreadConnection connection;

    public AdvancedMessageHandling(SpreadConnection connection) {
        this.connection = connection;
    }

    @Override
    public void regularMessageReceived(SpreadMessage spreadMessage) {
        System.out.println("Regular ThreadID:" + Thread.currentThread().getId());
        System.out.println("The message is: " + new String(spreadMessage.getData()));
    }

    @Override
    public void membershipMessageReceived(SpreadMessage spreadMessage) {
        System.out.println("MemberShip ThreadID:" + Thread.currentThread().getId());
        MembershipInfo info = spreadMessage.getMembershipInfo();
        if (info.isSelfLeave()) {
            System.out.println("Left group:"+info.getGroup().toString());
        } else {
        //if (info.getMembers() != null) {
            SpreadGroup[] members = info.getMembers();
            System.out.println("members of belonging group:"+info.getGroup().toString());
            for (int i = 0; i < members.length; ++i) {
                System.out.print(members[i] + ":");
            }
            System.out.println();
        }
    }
}
