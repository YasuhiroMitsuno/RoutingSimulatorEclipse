package network.datagram.L3;

import network.datagram.L2.Frame;
import network.protocol.L3.IPv4;

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
		setHardwareType((short)0x0001);
		setProtocolType((short)0x0800);
		setHardwareLength((byte)0x06);
		setProtocolLength((byte)0x04);
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
        this.bytes[0] = (byte)(hardwareType >> 8 & 0xFF);
        this.bytes[1] = (byte)(hardwareType & 0xFF);
	}
	public short getProtocolType() {
		return protocolType;
	}
	public void setProtocolType(short protocolType) {
		this.protocolType = protocolType;
        this.bytes[2] = (byte)(protocolType >> 8 & 0xFF);
        this.bytes[3] = (byte)(protocolType & 0xFF);
	}
	public byte getHardwareLength() {
		return hardwareLength;
	}
	public void setHardwareLength(byte hardwareLength) {
		this.hardwareLength = hardwareLength;
		this.bytes[4] = hardwareLength;
	}
	public byte getProtocolLength() {
		return protocolLength;
	}
	public void setProtocolLength(byte protocolLength) {
		this.protocolLength = protocolLength;
		this.bytes[5] = protocolLength;
	}
	public short getOperation() {
		return operation;
	}
	public void setOperation(short operation) {
		this.operation = operation;
        this.bytes[6] = (byte)(operation >> 8 & 0xFF);
        this.bytes[7] = (byte)(operation & 0xFF);
	}
	public long getSourceHardwareAddress() {
		return sourceHardwareAddress;
	}
	public void setSourceHardwareAddress(long sourceHardwareAddress) {
		this.sourceHardwareAddress = sourceHardwareAddress;
        this.bytes[8] = (byte)(sourceHardwareAddress >> 40 & 0xFF);
        this.bytes[9] = (byte)(sourceHardwareAddress >> 32 & 0xFF);
        this.bytes[10] = (byte)(sourceHardwareAddress >> 24 & 0xFF);
        this.bytes[11] = (byte)(sourceHardwareAddress >> 16 & 0xFF);
        this.bytes[12] = (byte)(sourceHardwareAddress >> 8 & 0xFF);
        this.bytes[13] = (byte)(sourceHardwareAddress & 0xFF);
	}
	public int getSourceProtocolAddress() {
		return sourceProtocolAddress;
	}
	public void setSourceProtocolAddress(int sourceProtocolAddress) {
		this.sourceProtocolAddress = sourceProtocolAddress;
        this.bytes[14] = (byte)(sourceProtocolAddress >> 24 & 0xFF);
        this.bytes[15] = (byte)(sourceProtocolAddress >> 16 & 0xFF);
        this.bytes[16] = (byte)(sourceProtocolAddress >> 8 & 0xFF);
        this.bytes[17] = (byte)(sourceProtocolAddress & 0xFF);
	}
	public long getDestHardwareAddress() {
		return destHardwareAddress;
	}
	public void setDestHardwareAddress(long destHardwareAddress) {
		this.destHardwareAddress = destHardwareAddress;
        this.bytes[18] = (byte)(destHardwareAddress >> 40 & 0xFF);
        this.bytes[19] = (byte)(destHardwareAddress >> 32 & 0xFF);
        this.bytes[20] = (byte)(destHardwareAddress >> 24 & 0xFF);
        this.bytes[21] = (byte)(destHardwareAddress >> 16 & 0xFF);
        this.bytes[22] = (byte)(destHardwareAddress >> 8 & 0xFF);
        this.bytes[23] = (byte)(destHardwareAddress & 0xFF);
	}
	public int getDestProtocolAddress() {
		return destProtocolAddress;
	}
	public void setDestProtocolAddress(int destProtocolAddress) {
		this.destProtocolAddress = destProtocolAddress;
        this.bytes[24] = (byte)(destProtocolAddress >> 24 & 0xFF);
        this.bytes[25] = (byte)(destProtocolAddress >> 16 & 0xFF);
        this.bytes[26] = (byte)(destProtocolAddress >> 8 & 0xFF);
        this.bytes[27] = (byte)(destProtocolAddress & 0xFF);
	}
    public String description() {
        String str = "";
        str += "Address Resolution Protocol";
        str += "\n\tHardware type: " + String.format("0x%04x", hardwareType);
        str += "\n\tProtocol type: " + String.format("0x%04x", protocolType);
        str += "\n\tHardware size: " + String.format("0x%04x", hardwareLength);
        str += "\n\tProtocol size: " + String.format("0x%04x", protocolLength);
        str += "\n\tOpcode: " + String.format("0x%04x", operation);
        str += "\n\tSender MAC address: " + Util.long2addr(sourceHardwareAddress);
        str += "\n\tSender IP address: " + Util.int2addr(sourceProtocolAddress);
        str += "\n\tTarget MAC address: " + Util.long2addr(destHardwareAddress);
        str += "\n\tTarget IP address: " + Util.int2addr(destProtocolAddress);                
        return str;
    }
}
