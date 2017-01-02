package network.device;

import network.datagram.L2.Util;
	
public class HubFactory extends DeviceFactory {
	private byte[] vendorCode;
	private byte[] nextProductCode;
	
	@Override
	protected Device createDevice() {
		Hub hub = new Hub();
		
		return null;
	}

}
