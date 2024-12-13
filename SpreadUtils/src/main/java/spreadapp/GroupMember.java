package spreadapp;

import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class GroupMember extends SpreadCommonMsg {

    private final Map<String, SpreadGroup> groupsBelonging = new HashMap<>(); //TODO is the getter relevant??

    public List<String> getNamesOfBelongingGroups() {
        return new ArrayList<>(groupsBelonging.keySet());
    }

    //private AdvancedMessageHandling advancedMsgHandling;
    public GroupMember(String user, String address, int port) {
        super(false);
        // Establish the spread connection.
        super.start(user, address, port);
    }

}
