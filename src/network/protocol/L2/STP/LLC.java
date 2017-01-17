package network.protocol.L2.STP;

import network.datagram.L2.LLCU;
import network.datagram.L2.STPFrame;

public class LLC {
	public static byte STP = (byte)0x42;
	
	public static LLCU llcuFromBPDU(STPFrame config) {
		LLCU llcu = new LLCU();
		llcu.setDsap(STP);
		llcu.setSsap(STP);
		llcu.setInformation(STP);
		llcu.setData(config.getBytes());
		return llcu;
	}
}
