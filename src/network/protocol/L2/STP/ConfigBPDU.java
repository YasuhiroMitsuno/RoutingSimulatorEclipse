package network.protocol.L2.STP;

import network.datagram.L2.STPFrame;
import network.datagram.L2.Util;

class ConfigBPDU {
	final static byte TOPOLOGY_CHANGE = (byte)0x01;
	final static byte PROPOSAL = (byte)0x02;
	final static byte PORT_ROLE = (byte)0x0C;
	final static byte LEARNING = (byte)0x10;
	final static byte FORWARDING = (byte)0x20;
	final static byte AGREEMENT = (byte)0x40;
	final static byte TOPOLOGY_CHANGE_ACK = (byte)0x80;
	int type;
    long rootId;    /* Root Identifier */
    long rootPathCost;    /* Path Cost */
    long bridgeId;  /* Bridge Identifier */
    int portId;       /* Port Identifier */
    int messageAge;   /* Message Age */
    int maxAge;       /* Max Age */
    int helloTime;    /* Hello Time */
    int forwardDelay; /* Forward Delay */
    boolean topologyChangeAcknowledgement;
    boolean topologyChange;
    
    ConfigBPDU() {
    	this.topologyChangeAcknowledgement = false;
    	this.topologyChange = false;
    }
    
    ConfigBPDU(STPFrame stpFrame) {
    	this.type = stpFrame.getMessageType();
    	this.rootId = Util.bytesToLong(stpFrame.getRootId(), 8);
    	this.rootPathCost = stpFrame.getPathCost();
    	this.bridgeId = Util.bytesToLong(stpFrame.getBridgeId(), 8);
    	this.portId = stpFrame.getPortId();
    	this.messageAge = stpFrame.getMessageAge();
    	this.maxAge = stpFrame.getMaxAge();
    	this.helloTime = stpFrame.getHelloTime();
    	this.forwardDelay = stpFrame.getForwardDelay();
    	int flags = stpFrame.getFlags();
    	this.topologyChangeAcknowledgement = (flags & TOPOLOGY_CHANGE_ACK) == 1;
    	this.topologyChange = (flags & TOPOLOGY_CHANGE) == 1;
    }
}
