package network.device;

import network.datagram.L2.Util;
	
public class L2SwitchFactory extends DeviceFactory {
	private byte[] vendorCode = {(byte)0x01, (byte)0x23, (byte)0x45};
	private byte[] nextProductCode;
	
	public L2SwitchFactory() {
		//vendorCode = new byte[3];
		nextProductCode = new byte[3];
	}
	
	@Override
	protected Device createDevice() {
		byte[] address = new byte[6];
		address[0] = vendorCode[0];
		address[1] = vendorCode[1];
		address[2] = vendorCode[2];
		address[3] = nextProductCode[0];
		address[4] = nextProductCode[1];
		address[5] = nextProductCode[2];

		nextProductCode[2] += 1;
		
		L2Switch bridge = new L2Switch(address);
		return bridge;
	}

	@Override
	public Device createDevice(double x, double y) {
		byte[] address = new byte[6];
		address[0] = vendorCode[0];
		address[1] = vendorCode[1];
		address[2] = vendorCode[2];
		address[3] = nextProductCode[0];
		address[4] = nextProductCode[1];
		address[5] = nextProductCode[2];

		nextProductCode[2] += 1;
		
		L2Switch bridge = new L2Switch(address, x, y);
		return bridge;
	}
	
}