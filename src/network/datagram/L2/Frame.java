package network.datagram.L2;

import network.datagram.L2.Util;

/*
  RFC3422(https://www.rfc-editor.org/rfc/rfc791.txt)に基づいたMACフレームクラス．

       0                   1                   2                   3
       0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
      +-+-+-+-+-+-+-+-+
      |  HDLC Flag    |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |      Address and Control      |      0xFE     |      0x31     |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |        (reserved)             |     Source MAPOS Address      |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |F|0|Z|0| Pads  |   MAC Type    |    Destination MAC Address    |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |                       Destination MAC Address                 |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |                       Source MAC Address                      |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |     Source MAC Address        |          Length/Type          |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |                    LLC data ...
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |                   LAN FCS (optional)                          |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |               potential line protocol pad                     |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |                   Frame FCS (16/32bits)                       |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

           Figure 3. 802.3 Frame format (IEEE 802 Un-tagged Frame)
 */

public class Frame {
    private byte[] bytes;       /* Binary Data */
    private byte HDLF_Flag;
    private long destination; /* Destination Address */
    private long source;      /* Source Address */
    private int length;
    
    private byte[] FCS; /* Frame Check Sequence */

    public static Frame getHeader(Frame frame) {
        byte[] bytes = new byte[14];
        System.arraycopy(frame.getBytes(), 0, bytes, 0, 14);
        return new Frame(bytes);
    }

    public Frame() {
        this.bytes = new byte[14];
    }

    public Frame(byte[] bytes) {
        this.bytes          = bytes;
        this.destination    = Util.byte2long(bytes, 0, 6);
        this.destination    = Util.byte2long(bytes, 6, 6);
        this.length         = (bytes[12] & 0xFF) << 8 | bytes[13] & 0xFF;
    }
    
    public void setDestination(long destination) {
        this.destination = destination;
        this.bytes[0] = (byte)(destination >> 40 & 0xFF);
        this.bytes[1] = (byte)(destination >> 32 & 0xFF);
        this.bytes[2] = (byte)(destination >> 24 & 0xFF);
        this.bytes[3] = (byte)(destination >> 16 & 0xFF);
        this.bytes[4] = (byte)(destination >> 8 & 0xFF);
        this.bytes[5] = (byte)(destination & 0xFF);
    }

    public void setDestination(String addr) {
        setDestination(Util.addr2long(addr));
    }

    public long getDestination() {
        return this.destination;
    }

    public void setSource(byte[] source) {
        this.source = Util.byte2long(source, 0, 6);
        this.bytes[6] = source[0];
        this.bytes[7] = source[1];
        this.bytes[8] = source[2];
        this.bytes[9] = source[3];
        this.bytes[10] = source[4];
        this.bytes[11] = source[5];
    }
    public void setSource(long source) {
        this.source = source;
        this.bytes[6] = (byte)(source >> 40 & 0xFF);
        this.bytes[7] = (byte)(source >> 32 & 0xFF);
        this.bytes[8] = (byte)(source >> 24 & 0xFF);
        this.bytes[9] = (byte)(source >> 16 & 0xFF);
        this.bytes[10] = (byte)(source >> 8 & 0xFF);
        this.bytes[11] = (byte)(source & 0xFF);
    }
    
    public void setSource(String addr) {
        setSource(addr2Bytes(addr));
    }

    public long getSource() {
        return this.source;
    }

    public void setData(byte[] data) {
        if (data.length >= 1536) {
            setLength(data.length);
        } else {
            setLength(14 + data.length);
        }
        byte[] newBytes;
        if (this.length >= 1536) {
            newBytes = new byte[14 + this.length];
        } else {
            newBytes = new byte[this.length];
        }
        /* Copy Header */
        System.arraycopy(this.bytes, 0, newBytes, 0, 14);
        System.arraycopy(data, 0, newBytes, 14, data.length);
        this.bytes = newBytes;
    }

    public byte[] getData() {
        int len;
        if (this.length >= 1536) {
            len = this.length;
        } else {
            len = this.length - 14;
        }
        byte[] data = new byte[len];
        System.arraycopy(this.bytes, 14, data, 0, len);
        return data;
    }

    public byte[] getBytes() {
        return this.bytes;
    }

    public String description() {
        String str = "";
        str += "MAC Frame";
        str += "\n\tDestination Address: " + Util.long2Addr(this.destination);
        str += "\n\tSource Address: " + Util.long2Addr(this.source);
        return str;
    }

    @Deprecated
    public void setLength(int length) {
        this.length = length & 0xFFFF;
        this.bytes[12] = (byte)(this.length >> 8 & 0xFF);
        this.bytes[13] = (byte)(this.length & 0xFF);
    }

    public int getLength() {
        return this.length;
    }

    private byte[] addr2Bytes(String addr) {
        String[] addrs = addr.split("\\:", 0);
        byte[] bytes = new byte[6];
        bytes[0] = (byte)Integer.parseInt(addrs[0], 16);
        bytes[1] = (byte)Integer.parseInt(addrs[1], 16);
        bytes[2] = (byte)Integer.parseInt(addrs[2], 16);
        bytes[3] = (byte)Integer.parseInt(addrs[3], 16);
        bytes[4] = (byte)Integer.parseInt(addrs[4], 16);
        bytes[5] = (byte)Integer.parseInt(addrs[5], 16);
        return bytes;
    }

    private String bytes2Addr(byte[] bytes) {
        String addr = String.format("%02x", (bytes[0] & 0xFF)) + ":" +
            String.format("%02x", (bytes[1] & 0xFF)) + ":" +
            String.format("%02x", (bytes[2] & 0xFF)) + ":" +
            String.format("%02x", (bytes[3] & 0xFF)) + ":" +
            String.format("%02x", (bytes[4] & 0xFF)) + ":" +
            String.format("%02x", (bytes[5] & 0xFF));
        return addr;
    }

}
