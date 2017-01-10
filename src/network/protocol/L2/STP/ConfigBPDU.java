package network.protocol.L2.STP;

import network.datagram.L2.Util;

class ConfigBPDU {
	final static int TOPOLOGY_CHANGE = 0x01;
	final static int PROPOSAL = 0x02;
	final static int PORT_ROLE = 0x0C;
	final static int LEARNING = 0x10;
	final static int FORWARDING = 0x20;
	final static int AGREEMENT = 0x40;
	final static int TOPOLOGY_CHANGE_ACK = 0x80;
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
