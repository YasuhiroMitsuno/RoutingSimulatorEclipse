package network.protocol.L2.STP;

import network.datagram.L2.Frame;
import network.datagram.L2.Util;

public class STPFrame {
	final static int ConfigBPDUType = 0;
	final static int TCNBPDUType = 128;
    private byte[] bytes;      /* Binary Data */
    private int protocolId;   /* Protocol ID */
    private int version;       /* Version */
    private int messageType;  /* Message Type */
    private int flags;         /* Flags */
    private byte[] rootId;    /* Root ID */
    private int rootBridgePriority; /* Root Bridge Priority */
    private byte[] rootBridgeAddress;
    private long pathCost;    /* Path Cost */
    private byte[] bridgeId;  /* Bridge ID */
    private int bridgePriority;     /* Bridge Priority */
    private byte[] bridgeAddress;
    private int portId;       /* Port ID */
    private int messageAge;   /* Message Age */
    private int maxAge;       /* Max Age */
    private int helloTime;    /* Hello Time */
    private int forwardDelay; /* Forward Delay */
    
    public STPFrame() {
        this.bytes = new byte[35];
        this.rootId = new byte[8];
        this.rootBridgeAddress = new byte[6];
        this.bridgeId = new byte[8];
        this.bridgeAddress = new byte[6];        
        setProtocolId(0);
        setVersion(0);
        setMessageType(0x00);
        setFlags(0x00);
        setPathCost(0);
        setMaxAge(20);
        setHelloTime(2);
        setForwardDelay(15);
    }
    
    public STPFrame(ConfigBPDU config) {
    	this();
    	setMessageType(ConfigBPDUType);
    	setRootId(Util.longToBytes(config.rootId, 8));
    	setPathCost(config.rootPathCost);
        setBridgeId(Util.longToBytes(config.bridgeId, 8));
        setPortId(config.portId);
        setMessageAge(config.messageAge);
        setMaxAge(config.maxAge);
        setHelloTime(config.helloTime);
        setForwardDelay(config.forwardDelay);
        setFlags(config.topologyChangeAcknowledgement? this.flags | 0x80: this.flags ^ 0x80); 
        setFlags(config.topologyChange? this.flags | 0x01: this.flags ^ 0x01);
    }
    
    public STPFrame(TcnBPDU tcn) {
    	this();
    	setMessageType(TCNBPDUType);
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
        this.rootId       = new byte[8];
        for (int i=0;i<8;i++) {
            this.rootId[i] = bytes[i+5];
        }
        this.rootBridgePriority = (bytes[5] & 0xFF) << 8 | bytes[6] & 0xFF;
        this.rootBridgeAddress = new byte[6];
        for (int i=0;i<6;i++) {
            this.rootBridgeAddress[i] = bytes[i+7];
        }
        this.pathCost     = (bytes[13] << 24 | bytes[14] << 16 | bytes[15] << 8 | bytes[16]);
        this.bridgeId     = new byte[8];
        for (int i=0;i<8;i++) {
            this.bridgeId[i] = bytes[i+17];
        }
        this.bridgePriority = (bytes[17] & 0xFF) << 8 | bytes[18] & 0xFF;
        this.bridgeAddress = new byte[6];
        for (int i=0;i<6;i++) {
            this.bridgeAddress[i] = bytes[i+19];
        }
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

    public void setRootId(byte[] rootId) {
        this.rootId = rootId;
        for (int i=0;i<8;i++) {
            this.bytes[i+5] = this.rootId[i];
        }
        this.rootBridgePriority = (rootId[0] & 0xFF) << 8 | rootId[1] & 0xFF;
        for (int i=0;i<6;i++) {
            this.rootBridgeAddress[i] = rootId[i+2];
        }
    }
    
    @Deprecated
    public void setRootBridgePriority(int priority) {
    	this.rootBridgePriority = priority & 0xFFFF;
    	this.bytes[5] = (byte)(priority >> 8 & 0xFF);
    	this.bytes[6] = (byte)(priority & 0xFF);
    }
    
    @Deprecated
    public void setRootBridgeAddress(byte[] addr) {
    	this.rootBridgeAddress = addr;
    	for (int i=0;i<6;i++) {
    		this.bytes[i+7] = addr[i];
    	}
    }
    
    @Deprecated
    public void setRootBridgeAddress(String addr) {
    	setRootBridgeAddress(Util.addr2Bytes(addr));
    }

    public void setPathCost(long pathCost) {
        this.pathCost = pathCost;
        this.bytes[13] = (byte)(this.pathCost >> 24 & 0xFF);
        this.bytes[14] = (byte)(this.pathCost >> 16 & 0xFF);
        this.bytes[15] = (byte)(this.pathCost >>  8 & 0xFF);
        this.bytes[16] = (byte)(this.pathCost >>  0 & 0xFF);
    }

    public void setBridgeId(byte[] bridgeId) {
        this.bridgeId = bridgeId;
        for (int i=0;i<8;i++) {
            this.bytes[i+17] = this.bridgeId[i];
        }
        this.bridgePriority = (bridgeId[0] & 0xFF) << 8 | bridgeId[1] & 0xFF;
        for (int i=0;i<6;i++) {
            this.bridgeAddress[i] = bridgeId[i+2];
        }
    }

    public void setBridgePriority(int priority) {
    	this.bridgePriority = priority & 0xFFFF;
    	this.bytes[17] = (byte)(priority >> 8 & 0xFF);
    	this.bytes[18] = (byte)(priority & 0xFF);
  	}
    
    public void setBridgeAddress(byte[] addr) {
    	this.bridgeAddress = addr;
    	for (int i=0;i<6;i++) {
    		this.bytes[i+19] = addr[i];
    	}
    }
    
    public void setBridgeAddress(String addr) {
    	setBridgeAddress(Util.addr2Bytes(addr));
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

    public byte[] getRootId() {
        return this.rootId;
    }
    
    public int getRootBridgePriority() {
    	return this.rootBridgePriority;
    }
    
    public byte[] getRootBridgeAddress() {
    	return this.rootBridgeAddress;
    }

    public long getPathCost() {
        return this.pathCost;
    }

    public byte[] getBridgeId() {
        return this.bridgeId;
    }
    
    public int getBridgePriority() {
    	return this.bridgePriority;
    }
    
    public byte[] getBridgeAddress() {
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
        str += "\n\tBPDU Type: " + this.messageType;
        str += "\n\tBPDU flags: " + String.format("0x%02x", this.flags);
        str += "\n\tRoot Identifier: " + this.rootBridgePriority + " / " + Util.bytes2Addr(this.rootBridgeAddress);
        str += "\n\tRoot Path Cost: " + this.pathCost;
        str += "\n\tBridge Identifier: " + this.bridgePriority + " / " + Util.bytes2Addr(this.bridgeAddress);
        str += "\n\tPort Identifier: " + String.format("0x%04x", this.portId);
        str += "\n\tMessage Age: " + this.messageAge;
        str += "\n\tMax Age: " + this.maxAge;
        str += "\n\tHello Time: " + this.helloTime;
        str += "\n\tForward Delay: " + this.forwardDelay;
        return str;
    }
}

