package network.datagram.L3;

import network.datagram.L2.Frame;
import network.protocol.L3.IPv4;

/*
  Internet Datagram
  RFC791 INTERNET PROTOCOL(https://www.rfc-editor.org/rfc/rfc791.txt)

   0                   1                   2                   3
   0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |Version|  IHL  |Type of Service|          Total Length         |
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |         Identification        |Flags|      Fragment Offset    |
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |  Time to Live |    Protocol   |         Header Checksum       |
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |                       Source Address                          |
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |                    Destination Address                        |
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |                    Options                    |    Padding    |
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

                  Example Internet Datagram Header
*/

public class Packet {
    private byte[] bytes;       /* Binary Data */
    private int version;        /* Version */
    private int IHL;            /* Private Internet Header Length */
    private int TOS;            /* Type of Service */
    private int length;         /* Total Length */
    private int id;             /* Identification */
    private int flags;          /* Flags */
    private int offset;         /* Offset */
    private int TTL;            /* Time to Live */
    private int protocol;       /* Protocol */
    private int checksum;       /* Header Checksum */
    private byte[] source;      /* Source Address */
    private byte[] destination; /* Destination Address */
    private int option;         /* Options */
    private int padding;        /* Padding */

    public static Packet getHeader(Packet packet) {
        byte[] bytes = new byte[20];
        System.arraycopy(packet.getBytes(), 0, bytes, 0, 20);
        return new Packet(bytes);
    }
    
    public Packet() {
        /* create minimum packet */
        this.bytes = new byte[20];
        setVersion(4);
        setIHL(5);
        setTOS(0x00);
        setLength(20);
        setId(0x0000);
        setFlags(0x00);
        setOffset(0);
        setTTL(0);
        setProtocol(0);
        setSource("0.0.0.0");
        setDestination("0.0.0.0");
    }

    public Packet(byte[] bytes) {
	_setBytes(bytes);
    }

    public Packet(Frame frame) {
	_setBytes(frame.getData());
    }

    private void _setBytes(byte[] bytes) {
        /* set binary value */
        this.bytes = bytes;
        /* set each parameter */
        this.version        = (bytes[0] & 0xF0) >> 4;
        this.IHL            = (bytes[0] & 0x0F);
        this.TOS            = (bytes[1] & 0xFF);
        this.length         = (bytes[2] & 0xFF) << 8 | bytes[3] & 0xFF;
        this.id             = (bytes[4] & 0xFF) << 8 | bytes[5] & 0xFF;
        this.flags          = (bytes[6] & 0xFF) >> 5;
        this.offset         = (bytes[6] & 0x1F) << 8 | bytes[7] & 0xFF;
        this.TTL            = (bytes[8] & 0xFF);
        this.protocol       = (bytes[9] & 0xFF);
        this.checksum       = (bytes[10] & 0xFF) << 8 | (bytes[11] & 0xFF);
        this.source         = new byte[4];
        this.source[0]      = bytes[12];
        this.source[1]      = bytes[13];
        this.source[2]      = bytes[14];
        this.source[3]      = bytes[15];
        this.destination    = new byte[4];
        this.destination[0] = bytes[16];
        this.destination[1] = bytes[17];
        this.destination[2] = bytes[18];
        this.destination[3] = bytes[19];
    }

    public void setVersion(int version) {
        this.version = version & 0x0F;
        this.bytes[0] = (byte)(this.version << 4 | this.bytes[0] & 0x0F);
        calcChecksum();
    }

    public void setIHL(int IHL) {
        this.IHL = IHL & 0x0F;
        this.bytes[0] = (byte)(this.bytes[0] & 0xF0 | this.IHL);
        calcChecksum();
    }

    public void setTOS(int TOS) {
        this.TOS = TOS & 0xFF;
        this.bytes[1] = (byte)this.TOS;
        calcChecksum();
    }

    @Deprecated
    public void setLength(int length) {
        this.length = length & 0xFFFF;
        this.bytes[2] = (byte)(this.length >> 8 & 0xFF);
        this.bytes[3] = (byte)(this.length & 0xFF);
        calcChecksum();
    }

    public void setId(int id) {
        this.id = id & 0xFFFF;
        this.bytes[4] = (byte)(this.id >> 8 & 0xFF);
        this.bytes[5] = (byte)(this.id & 0xFF);
        calcChecksum();
    }

    public void setFlags(int flags) {
        this.flags = flags & 0x03;
        this.bytes[6] = (byte)(this.flags << 5 | (this.bytes[6] & 0x1F));
        calcChecksum();
    }

    public void setOffset(int offset) {
        this.offset = offset & 0x1FFF;
        this.bytes[6] = (byte)(this.bytes[6] & 0xE0 | this.offset >> 8 & 0x1F);
        this.bytes[7] = (byte)(this.offset & 0xFF);
        calcChecksum();
    }

    public void setTTL(int TTL) {
        this.TTL = TTL & 0xFF;
        this.bytes[8] = (byte)this.TTL;
        calcChecksum();
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol & 0xFF;
        this.bytes[9] = (byte)this.protocol;
        calcChecksum();
    }

    @Deprecated
    public void setChecksum(int checksum) {
        this.checksum = checksum & 0xFFFF;
        this.bytes[10] = (byte)(this.checksum >> 8 & 0xFF);
        this.bytes[11] = (byte)(this.checksum & 0xFF);
    }

    public void setSource(byte[] source) {
        this.source = source;
        this.bytes[12] = this.source[0];
        this.bytes[13] = this.source[1];
        this.bytes[14] = this.source[2];
        this.bytes[15] = this.source[3];
        calcChecksum();
    }

    public void setSource(String addr) {
        setSource(addr2Bytes(addr));
    }

    public byte[] getSource() {
        return this.source;
    }

    public int getFlags() {
        return this.flags;
    }

    public int getOffset() {
        return this.offset;
    }
    
    public int getTTL() {
    	return this.TTL;
    }

    public void setDestination(byte[] destination) {
        this.destination = destination;
        this.bytes[16] = this.destination[0];
        this.bytes[17] = this.destination[1];
        this.bytes[18] = this.destination[2];
        this.bytes[19] = this.destination[3];
        calcChecksum();
    }

    public void setDestination(String addr) {
        setDestination(addr2Bytes(addr));
    }

    public byte[] getDestination() {
        return this.destination;
    }
    

    public void setData(byte[] data) {
        setLength(this.IHL * 4 + data.length);
        byte[] newBytes = new byte[this.length];
        System.arraycopy(this.bytes, 0, newBytes, 0, this.IHL * 4);
        System.arraycopy(data, 0, newBytes, this.IHL * 4, data.length);
        this.bytes = newBytes;
    }

    public byte[] getData() {
        int len = this.bytes.length - this.IHL * 4;
        byte[] data = new byte[len];
        System.arraycopy(bytes, this.IHL * 4, data, 0, len);
        return data;
    }

    public String description() {
        String str = "";
        str += "Internet Protocol";
        str += "\n\tVersion: " + this.version;
        str += "\n\tHeader Length: " + this.IHL * 4 + " bytes";
        str += "\n\tType of Service: " + String.format("0x%02x", this.TOS);
        str += "\n\tTotal Length: " + this.length;
        str += "\n\tIdentificatin: " + String.format("0x%1$04x (%1$d)", this.id);
        str += "\n\tFlags: " + String.format("0x%02x", this.flags);
        str += "\n\tFlagment Offset: " + this.offset;
        str += "\n\tTime to Live: " + this.TTL;
        str += "\n\tProtocol: " + IPv4.Protocol.getName(this.protocol) + " (" + this.protocol + ")";
        str += "\n\tHeader Checksum: " + String.format("0x%04x", this.checksum);
        str += "\n\tSource Address: " + bytes2Addr(this.source);
        str += "\n\tDestination Address: " + bytes2Addr(this.destination);
        return str;
    }

    public int verifyChecksum() {
        long sum = 0;
        for (int i=0; i<this.IHL*2; i++) {
            sum += (bytes[i*2] & 0xFF) << 8 | bytes[i*2+1] & 0xFF;
        }
        while(sum >> 16 != 0) {
            sum = (sum & 0xFFFF) + (sum >> 16 & 0xFFFF);
        }
        return ~(int)sum & 0xFFFF;
    }

    public int getProtocol() {
        return this.protocol;
    }

    public byte[] getBytes() {
        return this.bytes;
    }

    public void D() {
        for(int i=0;i<this.bytes.length; i++) {
            System.out.print(String.format("%02x ", bytes[i]));
        }
    }
    
    /* private methods */

    private void calcChecksum() {
        /* Claclate Header Checksum */
        this.bytes[10] = this.bytes[11] = 0x00;
        this.checksum = verifyChecksum();
        this.bytes[10] = (byte)(this.checksum >> 8 & 0xFF);
        this.bytes[11] = (byte)(this.checksum & 0xFF);
    }

    private byte[] addr2Bytes(String addr) {
        String[] addrs = addr.split("\\.", 0);
        byte[] bytes = new byte[4];
        bytes[0] = (byte)Integer.parseInt(addrs[0]);
        bytes[1] = (byte)Integer.parseInt(addrs[1]);
        bytes[2] = (byte)Integer.parseInt(addrs[2]);
        bytes[3] = (byte)Integer.parseInt(addrs[3]);
        return bytes;
    }

    private String bytes2Addr(byte[] bytes) {
        String addr = (bytes[0] & 0xFF) + "." + (bytes[1] & 0xFF) + "." +
            (bytes[2] & 0xFF) + "." + (bytes[3] & 0xFF);
        return addr;
    }
}
