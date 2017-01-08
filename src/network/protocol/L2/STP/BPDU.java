package network.protocol.L2.STP;

class BPDU {
	int type;
    byte[] rootId;    /* Root Identifier */
    long rootPathCost;    /* Path Cost */
    byte[] bridgeId;  /* Bridge Identifier */
    int portId;       /* Port Identifier */
    int messageAge;   /* Message Age */
    int maxAge;       /* Max Age */
    int helloTime;    /* Hello Time */
    int forwardDelay; /* Forward Delay */
    boolean topologyChangeAcknowledgement;
    boolean topologyChange;
}
