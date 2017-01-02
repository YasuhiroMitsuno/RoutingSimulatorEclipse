/*
  Transmission Control Protocol Segment
  RFC793 TRANSMISSION CONTROL PROTOCOL(https://www.rfc-editor.org/rfc/rfc793.txt)

    0                   1                   2                   3
    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |          Source Port          |       Destination Port        |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |                        Sequence Number                        |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |                    Acknowledgment Number                      |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |  Data |           |U|A|P|R|S|F|                               |
   | Offset| Reserved  |R|C|S|S|Y|I|            Window             |
   |       |           |G|K|H|T|N|N|                               |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |           Checksum            |         Urgent Pointer        |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |                    Options                    |    Padding    |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |                             data                              |
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

                            TCP Header Format
*/
package network.datagram.L4;

import network.datagram.L3.Packet;

public class TCPSegment {
    private byte[] bytes;
    private int sourcePort;
    private int destinationPort;
    private long SEQ;
    private long ACKNUM;
    private int offset;
    private int reserved;
    private int flags;
    private int window;
    private int checksum;
    private int pointer;

    private byte[] pseudoSource;      /* Pseudo Source Address */
    private byte[] pseudoDestination; /* Pseudo Destination Address */

    public TCPSegment() {
        /* create minimum segment */
        this.bytes = new byte[40];
        setSourcePort(23);
        setDestinationPort(23);
        setOffset(5);
    }

    public TCPSegment(Packet packet) {
        _setBytes(packet.getData());
        /* For pseudo header */
        pseudoSource = packet.getSource();
        pseudoDestination = packet.getDestination();
    }

    @Deprecated
    public TCPSegment(byte[] bytes) {
        _setBytes(bytes);
    }

    private void _setBytes(byte[] bytes) {
        /* set binary value */
        this.bytes = bytes;
        /* set each parameter */
        this.sourcePort      = (bytes[0] & 0xFF) << 8 | bytes[1] & 0xFF;
        this.destinationPort = (bytes[2] & 0xFF) << 8 | bytes[3] & 0xFF;
        this.SEQ             = ((long)bytes[4] & 0xFF) << 24 | ((long)bytes[5] & 0xFF) << 16 |
                               ((long)bytes[6] & 0xFF) <<  8 | ((long)bytes[7] & 0xFF);
        this.ACKNUM          = ((long)bytes[8] & 0xFF) << 24 | ((long)bytes[9] & 0xFF) << 16 |
                               ((long)bytes[10] & 0xFF) <<  8 | ((long)bytes[11] & 0xFF);
        this.offset          = (bytes[12] >> 4);
        this.reserved        = (bytes[12] & 0x0F) << 2 | (bytes[13] >> 6);
        this.flags           = (bytes[13] & 0x3F);
        this.window          = (bytes[14] & 0xFF) << 8 | bytes[15] & 0xFF;
        this.checksum        = (bytes[16] & 0xFF) << 8 | bytes[17] & 0xFF;
        this.pointer         = (bytes[19] & 0xFF) << 8 | bytes[18] & 0xFF;
        /* For pseudo header */
        pseudoSource = new byte[4];
        pseudoDestination = new byte[4];
    }

    public void setSourcePort(int sourcePort) {
        this.sourcePort = sourcePort & 0xFFFF;
        this.bytes[0] = (byte)(this.sourcePort >> 8 & 0xFF);
        this.bytes[1] = (byte)(this.sourcePort & 0xFF);
    }

    public void setDestinationPort(int destinationPort) {
        this.destinationPort = destinationPort & 0xFFFF;
        this.bytes[2] = (byte)(this.destinationPort >> 8 & 0xFF);
        this.bytes[3] = (byte)(this.destinationPort & 0xFF);
    }

    public void setSEQ(long SEQ) {
        this.SEQ = SEQ & 0xFFFFFFFF;
        this.bytes[4] = (byte)(this.SEQ >> 24 & 0xFF);
        this.bytes[5] = (byte)(this.SEQ >> 16 & 0xFF);
        this.bytes[6] = (byte)(this.SEQ >>  8 & 0xFF);
        this.bytes[7] = (byte)(this.SEQ & 0xFF);
    }

    public void setACKNUM(long ACKNUM) {
        this.ACKNUM = ACKNUM & 0xFFFFFFFF;
        this.bytes[8]  = (byte)(this.ACKNUM >> 24 & 0xFF);
        this.bytes[9]  = (byte)(this.ACKNUM >> 16 & 0xFF);
        this.bytes[10] = (byte)(this.ACKNUM >>  8 & 0xFF);
        this.bytes[11] = (byte)(this.ACKNUM & 0xFF);
    }

    public void setOffset(int offset) {
        this.offset = offset & 0xFF;
        this.bytes[12] = (byte)((this.offset & 0x0F) << 4 | this.bytes[12] & 0x0F);
    }

    public void setFlags(int flags) {
        this.flags = flags & 0x3F;
        this.bytes[13] = (byte)(this.bytes[13] & 0xC0 | this.flags & 0x3F);
    }

    public void setWindow(int window) {
        this.window = window & 0xFFFF;
        this.bytes[14] = (byte)((this.window >> 8) & 0xFF);
        this.bytes[15] = (byte)(this.window & 0xFF);
    }

    public void setChecksum(int checksum) {
        this.checksum = checksum & 0xFFFF;
        this.bytes[16] = (byte)((this.checksum >> 8) & 0xFF);
        this.bytes[17] = (byte)(this.checksum & 0xFF);
    }

    public void setPointer(int pointer) {
        this.pointer = pointer & 0xFFFF;
        this.bytes[18] = (byte)((this.pointer >> 8) & 0xFF);
        this.bytes[19] = (byte)(this.pointer & 0xFF);
    }

    public void setData(byte[] data) {
        byte[] newBytes = new byte[this.offset * 4 + data.length];
        System.arraycopy(this.bytes, 0, newBytes, 0, this.offset * 4);
        System.arraycopy(data, 0, newBytes, this.offset * 4, data.length);
        this.bytes = newBytes;
    }

    public byte[] getBytes() {
        return this.bytes;
    }

    public String description() {
        String str = "";
        str += "Transmission Control Protocol";
        str += "\n\tSource Port: " + this.sourcePort;
        str += "\n\tDestination Port: " + this.destinationPort;
        str += "\n\t[TCP Segment Len: " + (this.bytes.length - this.offset * 4) + "]";
        str += "\n\tSequence number: " + this.SEQ;
        str += "\n\tAcknowledgment: " + this.ACKNUM;
        str += "\n\tHeader Length: " + this.offset * 4 + " bytes";
        str += "\n\tFlags: " + String.format("0x%02x (XXX)", this.flags);
        str += "\n\tWindow size value: " + this.window;
        str += "\n\tChecksum: " + String.format("0x%04x", this.checksum);
        str += "\n\tUrgent pointer: " + this.pointer;
        return str;
    }

    public void D(byte[] data) {
        for(int i=0;i<data.length; i++) {
            System.out.print(String.format("%02x ", data[i]));
        }
        System.out.println();
    }

    public int verifyChecksum() {
        /* Prepare pseudo header */
        byte[] pseudoHeader = new byte[12];
        System.arraycopy(this.pseudoSource, 0, pseudoHeader, 0, 4);
        System.arraycopy(this.pseudoDestination, 0, pseudoHeader, 4, 4);
        pseudoHeader[8] = 0;
        pseudoHeader[9] = 6;
        pseudoHeader[10] = (byte)(this.bytes.length >> 8 & 0xFF);
        pseudoHeader[11] = (byte)(this.bytes.length & 0xFF);

        /* Combine pseudo header and TCP segment */
        byte[] data = new byte[12 + ((this.bytes.length+1)/2)*2];
        System.arraycopy(pseudoHeader, 0, data, 0, 12);
        System.arraycopy(this.bytes, 0, data, 12, this.bytes.length);

        /* verify */
        long sum = 0;
        for (int i=0; i<data.length/2; i++) {
            sum += (data[i*2] & 0xFF) << 8 | data[i*2+1] & 0xFF;
        }
        while(sum >> 16 != 0) {
            sum = (sum & 0xFFFF) + (sum >> 16 & 0xFFFF);
        }
        return ~(int)sum & 0xFFFF;
    }

    public void calcChecksum() {
        /* Claclate Checksum */
        this.bytes[16] = this.bytes[17] = 0x00;
        this.checksum = verifyChecksum();
        this.bytes[16] = (byte)(this.checksum >> 8 & 0xFF);
        this.bytes[17] = (byte)(this.checksum & 0xFF);
    }

    /* private methods */

    private void setReserved() {
        this.reserved = 0x00;
        this.bytes[12] = (byte)(this.bytes[12] & 0xF0 | this.reserved >> 2 & 0x0F);
        this.bytes[13] = (byte)((this.reserved & 0x03) << 6 | this.bytes[13] & 0x3F);
    }
}
