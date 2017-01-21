package network.datagram.L3;

import network.protocol.L3.IPv4;

public class ICMPDatagram {
//	public static String[] format = new String[16];
	
	
	private byte[] bytes;
	private byte type;
	private byte code;
	private int checksum;
	
	public ICMPDatagram() {
		bytes = new byte[4];
	}
	
	public ICMPDatagram(byte[] bytes) {
		setBytes(bytes);
	}
	
	public ICMPDatagram(Packet packet) {
		this(packet.getData());
	}
	
	public byte[] getBytes() {
		return bytes;
	}
	
	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
		this.type = bytes[0];
		this.code = bytes[1];
		this.checksum = bytes[2] << 8 | bytes[3];
	}
	
	public byte getType() {
		return type;
	}
	
	public void setType(byte type) {
		this.type = type;
		this.bytes[0] = type;
	}
	
	public byte getCode() {
		return code;
	}
	
	public void setCode(byte code) {
		this.code = code;
		this.bytes[1] = code;
	}
	
	public int getChecksum() {
		return checksum;
	}
	
	public void setChecksum(int checksum) {
		this.checksum = checksum;
		this.bytes[2] = (byte)(checksum >> 8 & 0xFF); 
		this.bytes[3] = (byte)(checksum & 0xFF); 
	}
	
	public byte[] getData() {
        int len = this.bytes.length - 4;
        byte[] data = new byte[len];
        System.arraycopy(bytes, 4, data, 0, len);
        return data;
	}
	
	public void setData(byte[] data) {
        byte[] newBytes = new byte[data.length + 4];
        System.arraycopy(this.bytes, 0, newBytes, 0, 4);
        System.arraycopy(data, 0, newBytes, 4, data.length);
        this.bytes = newBytes;
	}
	
    public String description() {
        String str = "";
        str += "Internet Control Message Protocol";
        str += "\n\tType: " + this.type;
        str += "\n\tCode: " + this.code;
        str += "\n\tChecksum: " + String.format("0x%04x", this.checksum);
        return str;
	}
}
