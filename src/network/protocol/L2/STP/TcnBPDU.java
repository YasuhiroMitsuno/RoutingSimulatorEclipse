package network.protocol.L2.STP;

import network.datagram.L2.STPFrame;

public class TcnBPDU {
    byte[] bytes;      /* Binary Data */
    byte protocolId;   /* Protocol ID */
    byte version;       /* Version */
    int type;
	
	TcnBPDU() {
		
	}
	
    TcnBPDU(STPFrame stpFrame) {
    	type = stpFrame.getMessageType();
    }
}
