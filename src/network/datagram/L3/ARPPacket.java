package network.datagram.L3;

import network.datagram.L2.Frame;

public class ARPPacket {
	private byte[] bytes;
	private short hardwareType;
	private short protocolType;
	private byte hardwareLength;
	private byte protocolLength;
	private short operation;
	private long sourceHardwareAddress;
	private int sourceProtocolAddress;
	private long destHardwareAddress;
	private int destProtocolAddress;
	
	public ARPPacket(Frame frame) {
		setBytes(frame.getData());
	}
	
	public ARPPacket() {
		bytes = new byte[28];
	}
	
	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
		hardwareType 	= Util.byte2short(bytes, 0);
		protocolType 	= Util.byte2short(bytes, 2);
		hardwareLength 	= bytes[4];
		protocolLength 	= bytes[5];
		operation 		= Util.byte2short(bytes, 6);
		sourceHardwareAddress	= Util.byte2long(bytes, 8, 6);
		sourceProtocolAddress	= Util.byte2int(bytes, 14);
		destHardwareAddress		= Util.byte2long(bytes, 18, 6);		
		destProtocolAddress		= Util.byte2int(bytes, 24);
	}
	
	public short getHardwareType() {
		return hardwareType;
	}
	public void setHardwareType(short hardwareType) {
		this.hardwareType = hardwareType;
	}
	public short getProtocolType() {
		return protocolType;
	}
	public void setProtocolType(short protocolType) {
		this.protocolType = protocolType;
	}
	public byte getHardwareLength() {
		return hardwareLength;
	}
	public void setHardwareLength(byte hardwareLength) {
		this.hardwareLength = hardwareLength;
	}
	public byte getProtocolLength() {
		return protocolLength;
	}
	public void setProtocolLength(byte protocolLength) {
		this.protocolLength = protocolLength;
	}
	public short getOperation() {
		return operation;
	}
	public void setOperation(short operation) {
		this.operation = operation;
	}
	public long getSourceHardwareAddress() {
		return sourceHardwareAddress;
	}
	public void setSourceHardwareAddress(long sourceHardwareAddress) {
		this.sourceHardwareAddress = sourceHardwareAddress;
	}
	public int getSourceProtocolAddress() {
		return sourceProtocolAddress;
	}
	public void setSourceProtocolAddress(int sourceProtocolAddress) {
		this.sourceProtocolAddress = sourceProtocolAddress;
	}
	public long getDestHardwareAddress() {
		return destHardwareAddress;
	}
	public void setDestHardwareAddress(long destHardwareAddress) {
		this.destHardwareAddress = destHardwareAddress;
	}
	public int getDestProtocolAddress() {
		return destProtocolAddress;
	}
	public void setDestProtocolAddress(int destProtocolAddress) {
		this.destProtocolAddress = destProtocolAddress;
	}
	
}
