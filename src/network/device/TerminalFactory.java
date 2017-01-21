package network.device;

import network.datagram.L2.Util;
	
public class TerminalFactory extends DeviceFactory {
	private byte[] vendorCode = {(byte)0x11, (byte)0x22, (byte)0x33};
	private byte[] nextProductCode;
	
	public TerminalFactory() {
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
		
		Terminal terminal = new Terminal(Util.byte2long(address, 0, 6));
		return terminal;
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
		
		Terminal terminal = new Terminal(Util.byte2long(address, 0, 6), x, y);
		return terminal;
	}
	
}
