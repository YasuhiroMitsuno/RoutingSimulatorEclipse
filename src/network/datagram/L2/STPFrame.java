package network.datagram.L2;

import network.protocol.L2.STP.TcnBPDU;

public class STPFrame {
	final static int CONFIG_BPDU_TYPE = 0;
	final static int TCN_BPDU_TYPE = 128;
    private byte[] bytes;      /* Binary Data */
    private int protocolId;   /* Protocol ID */
    private int version;       /* Version */
    private int messageType;  /* Message Type */
    private int flags;         /* Flags */
    private long rootId;    /* Root ID */
    private int rootBridgePriority; /* Root Bridge Priority */
    private long rootBridgeAddress;
    private long pathCost;    /* Path Cost */
    private long bridgeId;  /* Bridge ID */
    private int bridgePriority;     /* Bridge Priority */
    private long bridgeAddress;
    private int portId;       /* Port ID */
    private int messageAge;   /* Message Age */
    private int maxAge;       /* Max Age */
    private int helloTime;    /* Hello Time */
    private int forwardDelay; /* Forward Delay */
    
    public STPFrame() {
        this.bytes = new byte[35];
        setProtocolId(0);
        setVersion(0);
        setMessageType(0x00);
        setFlags(0x00);
        setPathCost(0);
        setMaxAge(20);
        setHelloTime(2);
        setForwardDelay(15);
    }
      
    public STPFrame(TcnBPDU tcn) {
    	this();
    	setMessageType(TCN_BPDU_TYPE);
    }

    public STPFrame(byte[] bytes) {
        _setBytes(bytes);
    }

    public STPFrame(Frame frame) {
    	_setBytes(frame.getData());
    }
    
    private void _setBytes(byte[] bytes) {
        /* set binary value */
        this.bytes = bytes;
        /* set each parameter */
        this.protocolId   = (bytes[0] & 0xFF) << 8 | bytes[1] & 0xFF;
        this.version       = bytes[2] & 0xFF;
        this.messageType  = bytes[3] & 0xFF;
        this.flags         = bytes[4] & 0xFF;
        this.rootId       = Util.byte2long(bytes, 5, 8);
        this.rootBridgePriority = (bytes[5] & 0xFF) << 8 | bytes[6] & 0xFF;
        this.rootBridgeAddress = Util.byte2long(bytes, 7, 6);
        this.pathCost     = (bytes[13] << 24 | bytes[14] << 16 | bytes[15] << 8 | bytes[16]);
        this.bridgeId     = Util.byte2long(bytes, 17, 8);
        this.bridgePriority = (bytes[17] & 0xFF) << 8 | bytes[18] & 0xFF;
        this.bridgeAddress = Util.byte2long(bytes, 19, 6);
        this.portId       = (bytes[25] & 0xFF) << 8 | bytes[26] & 0xFF;
        this.messageAge   = (bytes[27] & 0xFF) << 8 | bytes[28] & 0xFF;
        this.maxAge       = (bytes[29] & 0xFF) << 8 | bytes[30] & 0xFF;
        this.helloTime    = (bytes[31] & 0xFF) << 8 | bytes[32] & 0xFF;
        this.forwardDelay = (bytes[33] & 0xFF) << 8 | bytes[34] & 0xFF;
    }

    public void setProtocolId(int protocolId) {
        this.protocolId = protocolId & 0xFFFF;
        this.bytes[0] = (byte)(this.protocolId >> 8);
        this.bytes[1] = (byte)(this.protocolId & 0xFF);
    }

    public void setVersion(int version) {
        this.version = version & 0xFF;
        this.bytes[2] = (byte)this.version;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType & 0xFF;
        this.bytes[3] = (byte)this.messageType;
    }

    public void setFlags(int flags) {
        this.flags = flags & 0xFF;
        this.bytes[4] = (byte)this.flags;
    }

    public void setRootId(long rootId) {
        this.rootId = rootId;
        this.rootBridgePriority = (int)(rootId >> 48 & 0xFFFF);
        this.rootBridgeAddress = (rootId << 16) >> 16;
        this.bytes[5] = (byte)(rootId >> 56 & 0xFF);
        this.bytes[6] = (byte)(rootId >> 48 & 0xFF);
        this.bytes[7] = (byte)(rootId >> 40 & 0xFF);
        this.bytes[8] = (byte)(rootId >> 32 & 0xFF);
        this.bytes[9] = (byte)(rootId >> 24 & 0xFF);
        this.bytes[10] = (byte)(rootId >> 16 & 0xFF);
        this.bytes[11] = (byte)(rootId >> 8 & 0xFF);
    	this.bytes[12] = (byte)(rootId & 0xFF);
    }
    
    @Deprecated
    public void setRootBridgePriority(int priority) {
    	this.rootBridgePriority = priority & 0xFFFF;
    	this.bytes[5] = (byte)(priority >> 8 & 0xFF);
    	this.bytes[6] = (byte)(priority & 0xFF);
    }
    
    @Deprecated
    public void setRootBridgeAddress(long addr) {
    	this.rootBridgeAddress = addr;
    	for (int i=0;i<6;i++) {
    		this.bytes[i+7] = (byte)((addr >> 8*(5-i)) & 0xFF);
    	}
    }
    
    @Deprecated
    public void setRootBridgeAddress(String addr) {
    	setRootBridgeAddress(Util.addr2long(addr));
    }

    public void setPathCost(long pathCost) {
        this.pathCost = pathCost;
        this.bytes[13] = (byte)(this.pathCost >> 24 & 0xFF);
        this.bytes[14] = (byte)(this.pathCost >> 16 & 0xFF);
        this.bytes[15] = (byte)(this.pathCost >>  8 & 0xFF);
        this.bytes[16] = (byte)(this.pathCost >>  0 & 0xFF);
    }

    public void setBridgeId(long bridgeId) {
        this.bridgeId = bridgeId;
        this.bridgePriority = (int)(bridgeId >> 48 & 0xFFFF);
        this.bridgeAddress = (bridgeId << 16) >> 16;
        this.bytes[17] = (byte)(bridgeId >> 56 & 0xFF);
        this.bytes[18] = (byte)(bridgeId >> 48 & 0xFF);
        this.bytes[19] = (byte)(bridgeId >> 40 & 0xFF);
        this.bytes[20] = (byte)(bridgeId >> 32 & 0xFF);
        this.bytes[21] = (byte)(bridgeId >> 24 & 0xFF);
        this.bytes[22] = (byte)(bridgeId >> 16 & 0xFF);
        this.bytes[23] = (byte)(bridgeId >> 8 & 0xFF);
    	this.bytes[24] = (byte)(bridgeId & 0xFF);
    }

    public void setBridgePriority(int priority) {
    	this.bridgePriority = priority & 0xFFFF;
    	this.bytes[17] = (byte)(priority >> 8 & 0xFF);
    	this.bytes[18] = (byte)(priority & 0xFF);
  	}
    
    public void setBridgeAddress(long addr) {
    	this.bridgeAddress = addr;
    }
    
    public void setBridgeAddress(String addr) {
    	setBridgeAddress(Util.addr2long(addr));
    }

    public void setPortId(int portId) {
        this.portId = portId & 0xFFFF;
        this.bytes[25] = (byte)(this.portId >> 8);
        this.bytes[26] = (byte)(this.portId & 0xFF);
    }

    public void setMessageAge(int messageAge) {
        this.messageAge = messageAge & 0xFFFF;
        this.bytes[27] = (byte)(this.messageAge >> 8);
        this.bytes[28] = (byte)(this.messageAge & 0xFF);
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge & 0xFFFF;
        this.bytes[29] = (byte)(this.maxAge >> 8);
        this.bytes[30] = (byte)(this.maxAge & 0xFF);
    }

    public void setHelloTime(int helloTime) {
        this.helloTime = helloTime & 0xFFFF;
        this.bytes[31] = (byte)(this.helloTime >> 8);
        this.bytes[32] = (byte)(this.helloTime & 0xFF);
    }

    public void setForwardDelay(int forwardDelay) {
        this.forwardDelay = forwardDelay & 0xFFFF;
        this.bytes[33] = (byte)(this.forwardDelay >> 8);
        this.bytes[34] = (byte)(this.forwardDelay & 0xFF);
    }
    
    public byte[] getBytes() {
    	return this.bytes;
    }

    public int getProtocolId() {
        return this.protocolId;
    }

    public int getVersion() {
        return this.version;
    }

    public int getMessageType() {
        return this.messageType;
    }

    public int getFlags() {
        return this.flags;
    }

    public long getRootId() {
        return this.rootId;
    }
    
    public int getRootBridgePriority() {
    	return this.rootBridgePriority;
    }
    
    public long getRootBridgeAddress() {
    	return this.rootBridgeAddress;
    }

    public long getPathCost() {
        return this.pathCost;
    }

    public long getBridgeId() {
        return this.bridgeId;
    }
    
    public int getBridgePriority() {
    	return this.bridgePriority;
    }
    
    public long getBridgeAddress() {
    	return this.bridgeAddress;
    }

    public int getPortId() {
        return this.portId;
    }

    public int getMessageAge() {
        return this.messageAge;
    }

    public int getMaxAge() {
        return this.maxAge;
    }

    public int getHelloTime() {
        return this.helloTime;
    }

    public int getForwardDelay() {
        return this.forwardDelay;
    }
    
    public String description() {
        String str = "";
        str += "Spanning Tree Protocol";
        str += "\n\tProtocol Identifier: " + String.format("0x%04x", this.protocolId);
        str += "\n\tProtocol Version Identifier: " + this.version;
        str += "\n\tBPDU Type: " + String.format("0x%02x", this.messageType);
        str += "\n\tBPDU flags: " + String.format("0x%02x", this.flags);
        str += "\n\tRoot Identifier: " + this.rootBridgePriority + " / " + Util.long2Addr(this.rootBridgeAddress);
        str += "\n\tRoot Path Cost: " + this.pathCost;
        str += "\n\tBridge Identifier: " + this.bridgePriority + " / " + Util.long2Addr(this.bridgeAddress);
        str += "\n\tPort Identifier: " + String.format("0x%04x", this.portId);
        str += "\n\tMessage Age: " + this.messageAge;
        str += "\n\tMax Age: " + this.maxAge;
        str += "\n\tHello Time: " + this.helloTime;
        str += "\n\tForward Delay: " + this.forwardDelay;
        return str;
    }
}

