package network.protocol.L2.STP;

import java.util.TimerTask;

import network.datagram.L2.Frame;
import network.datagram.L2.LLCU;
import network.datagram.L2.STPFrame;
import network.datagram.L2.Util;
import network.device.Device;
import network.protocol.L2.STP.STP.State;

public class STP {
	private Device delegate = null;
	private java.util.Timer timer;
	private boolean enabled;
	
    /* From IEEE Standard 802.1D 1998 Edition */	
	public enum State {
        DISABLED, LISTENING, LEARNING, FORWARDING, BLOCKING
	}
	final static byte CONFIG_BPDU_TYPE = (byte)0x00;
	final static byte TCN_BPDU_TYPE = (byte)0x80;
	final static int ZERO = 0;	
	final static int ONE = 1;
	final static int NO_PORT = 0;
	final static int NO_Of_PORTS = 6;
	final static int ALL_PORTS = NO_Of_PORTS + 1;
	final static int DEFAULT_PATH_COST = 10;
	final static int MESSAGE_AGE_INCREMENT = 1;
	
	private BridgeData bridgeInfo;
	private PortData[] portInfo;
	private ConfigBPDU[] configBPDU;
	private TcnBPDU[] tcnBPDU;
	private Timer helloTimer;
	private Timer tcnTimer;
	private Timer topologyChangeTimer;
	private Timer[] messageAgeTimer;
	private Timer[] forwardDelayTimer;
	private Timer[] holdTimer;
        
	public STP(Device delegate) {
		this.delegate = delegate;
    	this.bridgeInfo = new BridgeData();
    	this.portInfo = new PortData[ALL_PORTS];
    	for (int portNo = ZERO; portNo <= NO_Of_PORTS; portNo++) {
    		this.portInfo[portNo] = new PortData();
    		this.portInfo[portNo].portId = portNo;
    	}
    	this.configBPDU = new ConfigBPDU[ALL_PORTS];
    	for (int portNo = ZERO; portNo <= NO_Of_PORTS; portNo++) {
    		this.configBPDU[portNo] = new ConfigBPDU();
    	}
    	this.tcnBPDU = new TcnBPDU[ALL_PORTS];
    	for (int portNo = ZERO; portNo <= NO_Of_PORTS; portNo++) {
    		this.tcnBPDU[portNo] = new TcnBPDU();
    	}	
    	this.helloTimer = new Timer();
    	this.tcnTimer = new Timer();
    	this.topologyChangeTimer = new Timer();
    	this.messageAgeTimer = new Timer[ALL_PORTS];
    	for (int portNo = ZERO; portNo <= NO_Of_PORTS; portNo++) {
    		this.messageAgeTimer[portNo] = new Timer();
    	}
    	this.forwardDelayTimer = new Timer[ALL_PORTS];
    	for (int portNo = ZERO; portNo <= NO_Of_PORTS; portNo++) {
    		this.forwardDelayTimer[portNo] = new Timer();
    	}
    	this.holdTimer = new Timer[ALL_PORTS];
    	for (int portNo = ZERO; portNo <= NO_Of_PORTS; portNo++) {
    		this.holdTimer[portNo] = new Timer();
    	}
    	
    	setBridgeAddress(delegate.getMACAddress());
    	
		initialisation();
		this.timer = new java.util.Timer();
		setEnabled(true);
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if (enabled) {
			timer.schedule(new TickTask(this), 5000, 1000);
		} else {
			timer.cancel();
		}
	}

	private void sendConfigBPDU(int portNo, ConfigBPDU config) {
		if (!enabled) return;
		STPFrame stpFrame = new STPFrame();
		stpFrame.setMessageType(CONFIG_BPDU_TYPE);
	    stpFrame.setRootId(config.rootId);
	    stpFrame.setPathCost(config.rootPathCost);
	    stpFrame.setBridgeId(config.bridgeId);
	    stpFrame.setPortId(config.portId);
	    stpFrame.setMessageAge(config.messageAge);
	    stpFrame.setMaxAge(config.maxAge);
	    stpFrame.setHelloTime(config.helloTime);
	    stpFrame.setForwardDelay(config.forwardDelay);
	    stpFrame.setFlags(config.topologyChangeAcknowledgement? stpFrame.getFlags() | 0x80: stpFrame.getFlags() ^ 0x80); 
	    stpFrame.setFlags(config.topologyChange? stpFrame.getFlags() | 0x01: stpFrame.getFlags() ^ 0x01);
		LLCU llcu = LLC.llcuFromBPDU(stpFrame);
		delegate.broadcastData(llcu, 0, portNo-1);
	}
	
	public void receivedBPDU(int portNo, STPFrame stpFrame) {
		if (!enabled) return;
		if (stpFrame.getMessageType() == CONFIG_BPDU_TYPE) {
			ConfigBPDU config = new ConfigBPDU(stpFrame);
			receivedConfigBPDU(portNo + 1, config);
		} else if (stpFrame.getMessageType() == TCN_BPDU_TYPE) {
			TcnBPDU tcn = new TcnBPDU(stpFrame);
			receivedTCNBPDU(portNo + 1, tcn);
		}
	}
	
	private void setBridgeAddress(long address) {
		bridgeInfo.bridgeId = (bridgeInfo.bridgeId & 0xFF) | ((address << 16) >> 16);
		System.out.println(String.format("SET BRIDGE 0x%016x", bridgeInfo.bridgeId));
	}
    
	public boolean willSendFrame(int portNo) {
		portNo += 1;
		return portInfo[portNo].state == State.FORWARDING;
	}
	
    private class TickTask extends TimerTask {
    	private STP delegate;
    	
    	TickTask(STP delegate) {
    		this.delegate = delegate;
    	}
    	
		public void run() { 
			delegate.tick();
		}
	}
	
    public State getState(int portNo) {
    	return portInfo[portNo + 1].state;
    }
    
    public boolean isRootPort(int portNo) {
    	//portInfo[portNo].des
    	return false;
    }
    
    public boolean isDesignatedPort(int portNo) {
    	portNo += 1;
    	return (portInfo[portNo].designatedRoot == bridgeInfo.designatedRoot &&  
    	portInfo[portNo].designatedBridge == bridgeInfo.bridgeId &&
    	portInfo[portNo].designatedPort == portInfo[portNo].portId);
    }
    
    /* Referenced from IEEE Standard 802.1D 1998 Edition */
    public void transmitConfig(int portNo) {
    	if (holdTimer[portNo].active) {
    		portInfo[portNo].configPending = true;
    	} else {
    		configBPDU[portNo].type = CONFIG_BPDU_TYPE;
    		configBPDU[portNo].rootId = bridgeInfo.designatedRoot;
	    	configBPDU[portNo].rootPathCost = bridgeInfo.rootPathCost;
	    	configBPDU[portNo].bridgeId = bridgeInfo.bridgeId;
	    	configBPDU[portNo].portId = portInfo[portNo].portId;
	    	if (rootBridge()) {
	    		configBPDU[portNo].messageAge = ZERO;
	    	} else {
	    		configBPDU[portNo].messageAge = messageAgeTimer[bridgeInfo.rootPort].value + MESSAGE_AGE_INCREMENT;
	    	}
	    	configBPDU[portNo].maxAge = bridgeInfo.maxAge;
	    	configBPDU[portNo].helloTime = bridgeInfo.helloTime;
	    	configBPDU[portNo].forwardDelay = bridgeInfo.forwardDelay;
	    	configBPDU[portNo].topologyChangeAcknowledgement = portInfo[portNo].topologyChangeAcknowledge;
	    	configBPDU[portNo].topologyChange = bridgeInfo.topologyChange;
	    	
	    	if (configBPDU[portNo].messageAge < bridgeInfo.maxAge) {
	    		portInfo[portNo].topologyChangeAcknowledge = false;
	    		portInfo[portNo].configPending = false;
	    		sendConfigBPDU(portNo, configBPDU[portNo]);
	    		startHoldTimer(portNo);
	    	}
    	}
    }
    
    public boolean rootBridge() {
    	return bridgeInfo.designatedRoot == bridgeInfo.bridgeId;
    }
    
    boolean supersedesPortInfo(int portNo, ConfigBPDU config) {
    	return ((config.rootId < portInfo[portNo].designatedRoot) ||
    			((config.rootId == portInfo[portNo].designatedRoot) &&
    			((config.rootPathCost < portInfo[portNo].designatedCost) ||
    			((config.rootPathCost == portInfo[portNo].designatedCost) &&
    			((config.bridgeId < portInfo[portNo].designatedBridge) ||
    				((config.bridgeId == portInfo[portNo].designatedBridge) &&
    				((config.bridgeId != bridgeInfo.bridgeId) || (config.portId <= portInfo[portNo].designatedPort))    					 )
    			)
    			)
    			)
    			)
    	);
    }
    
    public void recordConfigurationInformation(int portNo, ConfigBPDU config) {
       	portInfo[portNo].designatedRoot = config.rootId;
    	portInfo[portNo].designatedCost = config.rootPathCost;
    	portInfo[portNo].designatedBridge = config.bridgeId;    	
    	portInfo[portNo].designatedPort = config.portId;
    	
    	startMessageAgeTimer(portNo, config.messageAge);
    }
    
    public void recordConfigurationTimeoutValues(ConfigBPDU config) {
    	bridgeInfo.maxAge = config.maxAge;
    	bridgeInfo.helloTime = config.helloTime;
    	bridgeInfo.forwardDelay = config.forwardDelay;
    	bridgeInfo.topologyChange = config.topologyChange;
    }
    
    private void configBPDUGeneration() {
    	for (int portNo = ONE; portNo <= NO_Of_PORTS; portNo++) {
    		if (designatedPort(portNo) && portInfo[portNo].state != State.DISABLED) {
    			transmitConfig(portNo);
    		}
    	}
    }
    
    private boolean designatedPort(int portNo) {
    	return portInfo[portNo].designatedBridge == bridgeInfo.bridgeId &&
    			portInfo[portNo].designatedPort == portInfo[portNo].portId;
    }
    
    private void reply(int portNo) {
    	transmitConfig(portNo);
    }
    
    private void transmitTCN() {
    	int portNo = bridgeInfo.rootPort;
    	tcnBPDU[portNo].type = TCN_BPDU_TYPE;
    	
    	sendTCNBPDU(portNo, tcnBPDU[bridgeInfo.rootPort]);
    }
    
    private void sendTCNBPDU(int portNo, TcnBPDU bpdu) {
		STPFrame stpFrame = new STPFrame(bpdu);
		LLCU llcu = LLC.llcuFromBPDU(stpFrame);
		Frame frame = new Frame();
		frame.setData(llcu.getBytes());
	//	delegate.sendFrame(portNo-1, frame);
	} 
    
    public void configurationUpdate() {
    	rootSelection();
    	
    	designatedPortSelection();
    	
    }
    
    private void rootSelection() {
    	int rootPort = NO_PORT;
    	
    	for (int portNo = ONE; portNo <= NO_Of_PORTS; portNo++) {
    		if ((!designatedPort(portNo) && (portInfo[portNo].state != State.DISABLED) && 
    				portInfo[portNo].designatedRoot < bridgeInfo.bridgeId ) &&
    				(rootPort == NO_PORT || portInfo[portNo].designatedRoot < portInfo[rootPort].designatedRoot) ||
    				( (portInfo[portNo].designatedRoot == portInfo[rootPort].designatedRoot) && 
    				(((portInfo[portNo].designatedCost + portInfo[portNo].pathCost) < (portInfo[rootPort].designatedCost + portInfo[rootPort].pathCost)) ||
    						(((portInfo[portNo].designatedCost + portInfo[portNo].pathCost) == (portInfo[rootPort].designatedCost + portInfo[rootPort].pathCost))
    								&&
    								(portInfo[portNo].designatedBridge < portInfo[rootPort].designatedBridge || 
    										(portInfo[portNo].designatedBridge == portInfo[rootPort].designatedBridge
    												&& ((portInfo[portNo].designatedPort < portInfo[rootPort].designatedPort) ||
    														((portInfo[portNo].designatedPort == portInfo[rootPort].designatedPort) && 
    																portInfo[portNo].portId < portInfo[rootPort].portId)
    								))
    				
    				))))) {
    			rootPort = portNo;
    		}
    	}
    	
    	bridgeInfo.rootPort = rootPort;
    	
    	if (rootPort == NO_PORT) {
    		bridgeInfo.designatedRoot = bridgeInfo.bridgeId;
    		bridgeInfo.rootPathCost = ZERO;
    	} else {
    		bridgeInfo.designatedRoot = portInfo[rootPort].designatedRoot;
    		bridgeInfo.rootPathCost = (portInfo[rootPort].designatedCost + portInfo[rootPort].pathCost);
    	}
    }
    
    private void designatedPortSelection() {
    	for (int portNo = ONE; portNo < NO_Of_PORTS; portNo++) {
    		if (designatedPort(portNo) || portInfo[portNo].designatedRoot != bridgeInfo.designatedRoot ||
    				bridgeInfo.rootPathCost < portInfo[portNo].designatedCost ||
    				((bridgeInfo.rootPathCost == portInfo[portNo].designatedCost) && (
    						bridgeInfo.bridgeId < portInfo[portNo].designatedBridge ||
    						( (bridgeInfo.bridgeId == portInfo[portNo].designatedBridge && 
    								(portInfo[portNo].portId <= portInfo[portNo].designatedPort)))))) {
    			becomeDesignatedPort(portNo);
    		}
    	}
    }
    
    private void becomeDesignatedPort(int portNo) {
    	portInfo[portNo].designatedRoot = bridgeInfo.designatedRoot;
    	portInfo[portNo].designatedCost = bridgeInfo.rootPathCost;
    	portInfo[portNo].designatedBridge = bridgeInfo.bridgeId;
    	portInfo[portNo].designatedPort = portInfo[portNo].portId;
    }
    
    private void portStateSelection() {
    	for (int portNo = ONE; portNo <= NO_Of_PORTS; portNo++) {
    		if (portNo == bridgeInfo.rootPort) {
    			portInfo[portNo].configPending = false;
    			portInfo[portNo].topologyChangeAcknowledge = false;
    			
    			makeForwarding(portNo);
    		} else if (designatedPort(portNo)) { 
    			stopMessageAgeTimer(portNo);
    			
    			makeForwarding(portNo);
    		} else {
    			portInfo[portNo].configPending = false;
    			portInfo[portNo].topologyChangeAcknowledge = false;
    			
    			makeBlocking(portNo);
    		}
    	}
    }
    
    private void makeForwarding(int portNo) {
    	if (portInfo[portNo].state == State.BLOCKING) {
    		setPortState(portNo, State.LISTENING);
    		
    		startForwardDelayTimer(portNo);
    	}
    }
    
    private void makeBlocking(int portNo) {
    	if (portInfo[portNo].state != State.DISABLED && portInfo[portNo].state != State.BLOCKING) {
    		if (portInfo[portNo].state == State.FORWARDING || portInfo[portNo].state == State.LEARNING) {
    			if (portInfo[portNo].changeDetectionEnabled == true) {
    				topologyChangeDetection();
    			}
    		}
    		setPortState(portNo, State.BLOCKING);

    		stopForwardDelayTimer(portNo);
    	}
    }
    
    private void setPortState(int portNo, State state) {
    	portInfo[portNo].state = state;
    }
	
    private void topologyChangeDetection() {
    	if (rootBridge()) {
    		bridgeInfo.topologyChange = true;

    		startTopologyChangeTimer();
    	} else if (bridgeInfo.topologyChangeDetected == false) {
    		transmitTCN();
    		
    		startTCNTimer();
    	}
    	
    	bridgeInfo.topologyChangeDetected = true;
    }
    
    private void topologyChangeAcknowledged() {
    	bridgeInfo.topologyChangeDetected = false;
    	stopTCNTimer();
    }
    
    private void acknowwledgeTopologyChange(int portNo) {
    	portInfo[portNo].topologyChangeAcknowledge = true;
    	
    	transmitConfig(portNo);
    }
    
    private void receivedConfigBPDU(int portNo, ConfigBPDU config) {
    	boolean root = rootBridge();
    	
    	if (portInfo[portNo].state != State.DISABLED) {
    		if (supersedesPortInfo(portNo, config)) {
    			recordConfigurationInformation(portNo, config);
    			
    			configurationUpdate();
    			
    			portStateSelection();
    			
    			if (!rootBridge() && root) {
    				stopHelloTimer();
    				
    				if (bridgeInfo.topologyChangeDetected) {
    					stopTopologyChangeTimer();
    					
    					transmitTCN();
    					
    					startTCNTimer();
    				}
    			}
    			
    			if (portNo == bridgeInfo.rootPort) {
    				recordConfigurationTimeoutValues(config);
    				
    				configBPDUGeneration();
    				
    				if (config.topologyChangeAcknowledgement) {
    					topologyChangeAcknowledged();
    				}
    			}
    		} else if (designatedPort(portNo)) {
    			reply(portNo);
    		}
    	}
    }
    
    private void receivedTCNBPDU(int portNo, TcnBPDU tcn) {
    	if (portInfo[portNo].state != State.DISABLED) {
    		if (designatedPort(portNo)) {
    			topologyChangeDetection();
    			
    			acknowwledgeTopologyChange(portNo);
    		}
    	}
    }
    
    private void helloTimerExpiry() {
    	configBPDUGeneration();
    	
    	startHelloTimer();
    }
    
    private void messageAgeTimerExpiry(int portNo) {
    	boolean root = rootBridge();
    	
    	becomeDesignatedPort(portNo);
    	
    	configurationUpdate();
    	
    	portStateSelection();
    	
    	if (rootBridge() && !root) {
    		bridgeInfo.maxAge = bridgeInfo.bridgeMaxAge;
    		bridgeInfo.helloTime = bridgeInfo.bridgeHelloTime;
    		bridgeInfo.forwardDelay = bridgeInfo.bridgeForwardDelay;
    		
    		topologyChangeDetection();
    		
    		stopTCNTimer();
    		
    		configBPDUGeneration();
    		
    		startHelloTimer();
    	}
    }
    
    private void forwardDelayTimerExpiry(int portNo) {
    	if (portInfo[portNo].state == State.LISTENING) {
    		setPortState(portNo, State.LEARNING);
    		
    		startForwardDelayTimer(portNo);
    	} else if (portInfo[portNo].state == State.LEARNING) {
    		setPortState(portNo, State.FORWARDING);
    		
    		if (designatedForSomePort()) {
    			if (portInfo[portNo].changeDetectionEnabled == true) {
    				topologyChangeDetection();
    			}
    		}
    	}
    }
    
    private boolean designatedForSomePort() {
    	for (int portNo = ONE; portNo < NO_Of_PORTS; portNo++) {
    		if (portInfo[portNo].designatedBridge == bridgeInfo.bridgeId) {
    			return true;
    		}
    	}
    	return false;
    }
    
    private void tcnTimerExpiry() {
    	transmitTCN();
    	
    	startTCNTimer();
    }
    
    private void topologyChangeTimerExpiry() {
    	bridgeInfo.topologyChangeDetected = false;
    	bridgeInfo.topologyChange = false;
    }
    
    private void holdTimerExpiry(int portNo) {
    	if (portInfo[portNo].configPending) {
    		transmitConfig(portNo);
    	}
    }
    
    private void initialisation() { 
    	bridgeInfo.designatedRoot = bridgeInfo.bridgeId;
    	bridgeInfo.rootPathCost = ZERO;
    	bridgeInfo.rootPort = NO_PORT;
    	
    	bridgeInfo.maxAge = bridgeInfo.bridgeMaxAge;
    	bridgeInfo.helloTime = bridgeInfo.bridgeHelloTime;
    	bridgeInfo.forwardDelay = bridgeInfo.bridgeForwardDelay;
    	
    	bridgeInfo.topologyChangeDetected = false;
    	bridgeInfo.topologyChange = false;
    	stopTCNTimer();
    	stopTopologyChangeTimer();
    	
    	for (int portNo = ONE; portNo <= NO_Of_PORTS; portNo++) {
    		initializePort(portNo);
    		setPortState(portNo, State.DISABLED);
    	}
    	portStateSelection();
    	configBPDUGeneration();
    	startHelloTimer();
    }
    
    private void initializePort(int portNo) {  	
    	becomeDesignatedPort(portNo);
    	
    	setPortState(portNo, State.BLOCKING);
    	
    	portInfo[portNo].topologyChangeAcknowledge = false;
    	
    	portInfo[portNo].configPending = false;
    	
    	portInfo[portNo].changeDetectionEnabled = true;
    	
    	stopMessageAgeTimer(portNo);
    	
    	stopForwardDelayTimer(portNo);
    	
    	stopHoldTimer(portNo);
    }
    
    public void enablePort(int portNo) {
    	if (!enabled) {
    		setPortState(portNo, State.FORWARDING);
    	} else {
    	initializePort(portNo);
    	
    	portStateSelection();
    	}
    }
    
    public void disablePort(int portNo) {
    	boolean root = rootBridge();
    	
    	becomeDesignatedPort(portNo);
    	
    	setPortState(portNo, State.DISABLED);
    	
    	portInfo[portNo].topologyChangeAcknowledge = false;
    	
    	portInfo[portNo].configPending = false;
    	
    	stopMessageAgeTimer(portNo);
    	
    	stopForwardDelayTimer(portNo);
    	
    	configurationUpdate();
    	
    	portStateSelection();
    	
    	if (rootBridge() && !root) {
    		bridgeInfo.maxAge = bridgeInfo.bridgeMaxAge;
    		bridgeInfo.helloTime = bridgeInfo.bridgeHelloTime;
    		bridgeInfo.forwardDelay = bridgeInfo.bridgeForwardDelay;
    		
    		topologyChangeDetection();
    		
    		stopTCNTimer();
    		
    		configBPDUGeneration();
    		
    		startHelloTimer();
    	}
    }
    
    private void setBridgePriority(long newBridgeId) {
    	boolean root = rootBridge();
    	
    	for (int portNo = ONE; portNo <= NO_Of_PORTS; portNo++) {
    		if (designatedPort(portNo)) {
    			portInfo[portNo].designatedBridge = newBridgeId;
    		}
    	}
    	
    	bridgeInfo.bridgeId = newBridgeId;
    	
    	configurationUpdate();
    	
    	portStateSelection();
    	
    	if (rootBridge() && !root) {
    		bridgeInfo.maxAge = bridgeInfo.bridgeMaxAge;
    		bridgeInfo.helloTime = bridgeInfo.bridgeHelloTime;
    		bridgeInfo.forwardDelay = bridgeInfo.bridgeForwardDelay;
    		
    		topologyChangeDetection();
    		
    		stopTCNTimer();
    		
    		configBPDUGeneration();
    		
    		startHelloTimer();
    	}
    }
    
    private void setPortPriority(int portNo, int newPortId) {
    	if (designatedPort(portNo)) {
    		portInfo[portNo].designatedPort = newPortId;
    	}
    	
    	portInfo[portNo].portId = newPortId;
    	
    	if (bridgeInfo.bridgeId == portInfo[portNo].designatedBridge && portInfo[portNo].portId < portInfo[portNo].designatedPort) {
    		becomeDesignatedPort(portNo);
    		
    		portStateSelection();
    	}
    }
    
    public void setPathCost(int portNo, long pathCost) {
    	portInfo[portNo].pathCost = pathCost;
    	
    	configurationUpdate();
    	
    	portStateSelection();
    }
    
    public void enableChangeDetection(int portNo) {
    	portInfo[portNo].changeDetectionEnabled = true;
    }
    
    public void disableChangeDtection(int portNo) {
    	portInfo[portNo].changeDetectionEnabled = false;
    }
    
    private void tick() {
    	if (helloTimerExpired()) {
    		helloTimerExpiry();
    	}
    	
    	if (tcnTimerExpired()) {
    		tcnTimerExpiry();
    	}
    	
    	if (topologyChangeTimerExpired()) {
    		topologyChangeTimerExpiry();
    	}
    	
    	for (int portNo = ONE; portNo <= NO_Of_PORTS; portNo++) {
    		if (messageAgeTimerExpired(portNo)) {
    			messageAgeTimerExpiry(portNo);
    		}
    	}
    	for (int portNo = ONE; portNo <= NO_Of_PORTS; portNo++) {
    		if (forwardDelayTimerExpired(portNo)) {
    			forwardDelayTimerExpiry(portNo);
    		}
    		if (holdTimerExpired(portNo)) {
    			holdTimerExpiry(portNo);
    		}
    	}
    }
    
    private void startHelloTimer() {
    	helloTimer.value = ZERO;
    	helloTimer.active = true;
    }
    
    private void stopHelloTimer() {
    	helloTimer.active = false;
    }
    
    private boolean helloTimerExpired() {
    	if (helloTimer.active && (++helloTimer.value >= bridgeInfo.helloTime)) {
    		helloTimer.active = false;
    		return true;
    	}
    	return false;
    }
    
    private void startTCNTimer() {
    	tcnTimer.value = ZERO;
    	tcnTimer.active = true;
    }
    
    private void stopTCNTimer() {
    	tcnTimer.active = false;
    }
    
    private boolean tcnTimerExpired() {
    	if (tcnTimer.active && (++tcnTimer.value >= bridgeInfo.bridgeHelloTime)) {
    		tcnTimer.active = false;
    		return true;
    	}
    	return false;
    }
    
    private void startTopologyChangeTimer() {
    	topologyChangeTimer.value = ZERO;
    	topologyChangeTimer.active = true;
    }
    
    private void stopTopologyChangeTimer() {
    	topologyChangeTimer.active = false;
    }
    
    private boolean topologyChangeTimerExpired() {
    	if (topologyChangeTimer.active && (++topologyChangeTimer.value >= bridgeInfo.topologyChangeTime)) {
    		topologyChangeTimer.active = false;
    		return true;
    	}
    	return false;
    }

    private void startMessageAgeTimer(int portNo, int messageAge) {
    	messageAgeTimer[portNo].value = messageAge;
    	messageAgeTimer[portNo].active = true;
    }
    
    private void stopMessageAgeTimer(int portNo) {
    	messageAgeTimer[portNo].active = false;
    }
    
    private boolean messageAgeTimerExpired(int portNo) {
    	if (messageAgeTimer[portNo].active && (++messageAgeTimer[portNo].value >= bridgeInfo.maxAge)) {
    		messageAgeTimer[portNo].active = false;
    		return true;
    	}
    	return false;
    }
    
    private void startForwardDelayTimer(int portNo) {
    	forwardDelayTimer[portNo].value = ZERO;
    	forwardDelayTimer[portNo].active = true;
    }
    
    private void stopForwardDelayTimer(int portNo) {
    	forwardDelayTimer[portNo].active = false;
    }
    
    private boolean forwardDelayTimerExpired(int portNo) {
    	if (forwardDelayTimer[portNo].active && (++forwardDelayTimer[portNo].value >= bridgeInfo.forwardDelay)) {
    		forwardDelayTimer[portNo].active = false;
    		return true;
    	}
    	return false;
    }
    
    private void startHoldTimer(int portNo) {
    	holdTimer[portNo].value = ZERO;
    	holdTimer[portNo].active = true;
    }
    
    private void stopHoldTimer(int portNo) {
    	holdTimer[portNo].active = false;
    }
    
    private boolean holdTimerExpired(int portNo) {
    	if (holdTimer[portNo].active && (++holdTimer[portNo].value >= bridgeInfo.holdTime)) {
    		holdTimer[portNo].active = false;
    		return true;
    	}
    	return false;
    }   

}     

class BridgeData {
	int type;
    long designatedRoot;
    long rootPathCost;    /* Path Cost */
    int rootPort;
    int maxAge = 20;       /* Max Age */
    int helloTime = 2;    /* Hello Time */
    int forwardDelay = 15; /* Forward Delay */
    long bridgeId;
    int bridgeMaxAge = 20;
    int bridgeHelloTime = 2;
    int bridgeForwardDelay = 15;
    boolean topologyChangeDetected;
    boolean topologyChange;
    int topologyChangeTime;
    int holdTime;
}

class PortData {
	int 			portId;
	State 	state;
	long 			pathCost;
	long 			designatedRoot;
	long 			designatedCost;
	long 			designatedBridge;
	int 			designatedPort;
	boolean			topologyChangeAcknowledge;
	boolean			configPending;
	boolean			changeDetectionEnabled;
}

class Timer {
	boolean active;
	int value;
}
