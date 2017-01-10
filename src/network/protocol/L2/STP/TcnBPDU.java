package network.protocol.L2.STP;

public class TcnBPDU {
	int type;
	
	TcnBPDU() {
		
	}
	
    TcnBPDU(STPFrame stpFrame) {
    	type = stpFrame.getMessageType();
    }
}
