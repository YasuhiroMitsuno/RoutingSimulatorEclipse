package network.protocol.L2.STP;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
//import java.util.Timer;
import java.util.TimerTask;

import network.datagram.L2.Frame;
import network.datagram.L2.Util;
import network.device.Device;
import network.device.Port;
import network.protocol.L2.STP.STP.State;

public class STP extends Thread {
	private Device delegate = null;
	private java.util.Timer timer;
	
    /* From IEEE Standard 802.1D 1998 Edition */	
	public enum State {
        Disabled, Listening, Learning, Forwarding, Blocking
	}
	final static int ConfigBPDUType = 0;
	final static int TCNBPDUType = 128;
	final static int Zero = 0;	
	final static int One = 1;
	final static int NoPort = 0;
	final static int NoOfPorts = 2;
	final static int AllPorts = NoOfPorts + 1;
	final static int DefaultPathCost = 10;
	final static int MessageAgeIncrement = 1;
	
	private BridgeData bridgeInfo;
	private PortData[] portInfo;
	private BPDU[] configBPDU;
	private BPDU[] tcnBPDU;
	private Timer helloTimer;
	private Timer tcnTimer;
	private Timer topologyChangeTimer;
	private Timer[] messageAgeTimer;
	private Timer[] forwardDelayTimer;
	private Timer[] holdTimer;
        
	public STP(Device delegate) {
		this.delegate = delegate;
    	this.bridgeInfo = new BridgeData();
    	this.portInfo = new PortData[AllPorts];
    	for (int portNo = Zero; portNo <= NoOfPorts; portNo++) {
    		this.portInfo[portNo] = new PortData();
    		this.portInfo[portNo].portId = portNo;
    	}
    	this.configBPDU = new BPDU[AllPorts];
    	for (int portNo = Zero; portNo <= NoOfPorts; portNo++) {
    		this.configBPDU[portNo] = new BPDU();
    	}
    	this.tcnBPDU = new BPDU[AllPorts];
    	for (int portNo = Zero; portNo <= NoOfPorts; portNo++) {
    		this.tcnBPDU[portNo] = new BPDU();
    	}	
    	this.helloTimer = new Timer();
    	this.tcnTimer = new Timer();
    	this.topologyChangeTimer = new Timer();
    	this.messageAgeTimer = new Timer[AllPorts];
    	for (int portNo = Zero; portNo <= NoOfPorts; portNo++) {
    		this.messageAgeTimer[portNo] = new Timer();
    	}
    	this.forwardDelayTimer = new Timer[AllPorts];
    	for (int portNo = Zero; portNo <= NoOfPorts; portNo++) {
    		this.forwardDelayTimer[portNo] = new Timer();
    	}
    	this.holdTimer = new Timer[AllPorts];
    	for (int portNo = Zero; portNo <= NoOfPorts; portNo++) {
    		this.holdTimer[portNo] = new Timer();
    	}
    	
    	setBridgeAddress(delegate.getMACAddress());
    	
		initialisation();
		this.timer = new java.util.Timer();
		timer.schedule(new TickTask(this), 1000, 1000);
	}
	
	private void sendConfigBPDU(int portNo, BPDU config) {
		
		STPFrame stpFrame = new STPFrame(config);
		Frame frame = new Frame();
		frame.setData(stpFrame.getBytes());
		delegate.sendFrame(portNo, frame);
	}
	
	public void receivedSTPFrame(int portNo, Frame frame) {

		STPFrame stpFrame = new STPFrame(frame);
		BPDU bpdu = new BPDU(stpFrame);
		if (bpdu.type == ConfigBPDUType) {
			receivedConfigBPDU(portNo, bpdu);
		} else if (bpdu.type == TCNBPDUType) {
			receivedTCNBPDU(portNo, bpdu);
		}
	}
	
	private void setBridgeAddress(byte[] address) {
		bridgeInfo.bridgeId = (bridgeInfo.bridgeId & 0xFF) | Util.bytesToLong(address, 6) << 16;
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
		
    /* Referenced from IEEE Standard 802.1D 1998 Edition */
    public void transmitConfig(int portNo) {
    	if (holdTimer[portNo].active) {
    		portInfo[portNo].configPending = true;
    	} else {
    		configBPDU[portNo].type = ConfigBPDUType;
    		configBPDU[portNo].rootId = bridgeInfo.designatedRoot;
	    	configBPDU[portNo].rootPathCost = bridgeInfo.rootPathCost;
	    	configBPDU[portNo].bridgeId = bridgeInfo.bridgeId;
	    	configBPDU[portNo].portId = portInfo[portNo].portId;
	    	if (rootBridge()) {
	    		configBPDU[portNo].messageAge = 0;
	    	} else {
	    		configBPDU[portNo].messageAge = messageAgeTimer[bridgeInfo.rootPort].value + MessageAgeIncrement;
	    	}
	    	configBPDU[portNo].maxAge = bridgeInfo.maxAge;
	    	configBPDU[portNo].helloTime = bridgeInfo.helloTime;
	    	configBPDU[portNo].forwardDelay = bridgeInfo.forwardDelay;
	    	configBPDU[portNo].topologyChangeAcknowledgement = this.portInfo[portNo].topologyChangeAcknowledge;
	    	configBPDU[portNo].topologyChange = bridgeInfo.topologyChange;
	    	
	    	if (configBPDU[portNo].messageAge < bridgeInfo.maxAge) {
	    		portInfo[portNo].topologyChangeAcknowledge = false;
	    		portInfo[portNo].configPending = false;
	    		sendConfigBPDU(portNo, configBPDU[portNo]);
	    		startHoldTimer(portNo);
	    	}
    	}
    }
    
    private boolean rootBridge() {
    	return bridgeInfo.designatedRoot == bridgeInfo.bridgeId;
    }
    
    boolean supersedesPortInfo(int portNo, BPDU config) {
    	return (config.rootId < portInfo[portNo].designatedRoot ||
    			(config.rootId == portInfo[portNo].designatedRoot &&
    			(config.rootPathCost < portInfo[portNo].designatedCost ||
    			(config.rootPathCost == portInfo[portNo].designatedCost &&
    			(config.bridgeId < portInfo[portNo].designatedBridge ||
    				(config.bridgeId == portInfo[portNo].designatedBridge &&
    				(config.bridgeId != bridgeInfo.bridgeId || config.portId <= portInfo[portNo].designatedPort)    					 )
    			)
    			)
    			)
    			)
    	);
    }
    
    public void recordConfigurationInformation(int portNo, BPDU config) {
       	portInfo[portNo].designatedBridge = config.rootId;
    	portInfo[portNo].designatedCost = config.rootPathCost;
    	portInfo[portNo].designatedBridge = config.bridgeId;    	
    	portInfo[portNo].designatedPort = config.portId;
    	
    	startMessageAgeTimer(portNo, config.messageAge);
    }
    
    public void recordConfigurationTimeoutValues(BPDU config) {
    	bridgeInfo.maxAge = config.maxAge;
    	bridgeInfo.helloTime = config.helloTime;
    	bridgeInfo.forwardDelay = config.forwardDelay;
    	bridgeInfo.topologyChange = config.topologyChange;
    }
    
    public void configBPDUGeneration() {
    	for (int portNo = One; portNo < NoOfPorts; portNo++) {
    		if (designatedPort(portNo) && portInfo[portNo].state != State.Disabled) {
    			transmitConfig(portNo);
    		}
    	}
    }
    
    private boolean designatedPort(int portNo) {
    	return portInfo[portNo].designatedBridge == bridgeInfo.bridgeId &&
    			portInfo[portNo].designatedPort == portInfo[portNo].portId;
    }
    
    public void reply(int portNo) {
    	transmitConfig(portNo);
    }
    
    public void transmitTCN() {
    	int portNo = bridgeInfo.rootPort;
    	tcnBPDU[portNo].type = TCNBPDUType;
    	
    	sendTCNBPDU(portNo, tcnBPDU[bridgeInfo.rootPort]);
    }
    
    private void sendTCNBPDU(int portNo, BPDU bpdu) {
    	
    }
    
    public void configurationUpdate() {
    	rootSelection();
    	
    	designatedPortSelection();
    	
    }
    
    private void rootSelection() {
    	int rootPort = NoPort;
    	
    	for (int portNo = One; portNo <= NoOfPorts; portNo++) {
    		if ((!designatedPort(portNo) && (portInfo[portNo].state != State.Disabled) && 
    				portInfo[portNo].designatedRoot < bridgeInfo.bridgeId ) &&
    				(rootPort == NoPort || portInfo[portNo].designatedRoot < portInfo[rootPort].designatedRoot) ||
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
    	
    	if (rootPort == NoPort) {
    		bridgeInfo.designatedRoot = bridgeInfo.bridgeId;
    		bridgeInfo.rootPathCost = Zero;
    	} else {
    		bridgeInfo.designatedRoot = portInfo[rootPort].designatedRoot;
    		bridgeInfo.rootPathCost = (portInfo[rootPort].designatedCost + portInfo[rootPort].pathCost);
    	}
    }
    
    private void designatedPortSelection() {
    	for (int portNo = One; portNo < NoOfPorts; portNo++) {
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
    	for (int portNo = One; portNo <= NoOfPorts; portNo++) {
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
    	if (portInfo[portNo].state == State.Blocking) {
    		setPortState(portNo, State.Listening);
    		
    		startForwardDelayTimer(portNo);
    	}
    }
    
    private void makeBlocking(int portNo) {
    	if (portInfo[portNo].state != State.Disabled && portInfo[portNo].state != State.Blocking) {
    		if (portInfo[portNo].state == State.Forwarding || portInfo[portNo].state == State.Learning) {
    			if (portInfo[portNo].changeDetectionEnabled == true) {
    				topologyChangeDetection();
    			}
    		}
    		setPortState(portNo, State.Blocking);

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
    
    private void receivedConfigBPDU(int portNo, BPDU config) {
    	boolean root = rootBridge();
    	
    	if (portInfo[portNo].state != State.Disabled) {
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
    
    private void receivedTCNBPDU(int portNo, BPDU tcn) {
    	if (portInfo[portNo].state != State.Disabled) {
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
    	if (portInfo[portNo].state == State.Listening) {
    		setPortState(portNo, State.Learning);
    		
    		startForwardDelayTimer(portNo);
    	} else if (portInfo[portNo].state == State.Learning) {
    		setPortState(portNo, State.Forwarding);
    		
    		if (designatedForSomePort()) {
    			if (portInfo[portNo].changeDetectionEnabled == true) {
    				topologyChangeDetection();
    			}
    		}
    	}
    }
    
    private boolean designatedForSomePort() {
    	for (int portNo = One; portNo < NoOfPorts; portNo++) {
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
    	bridgeInfo.rootPathCost = Zero;
    	bridgeInfo.rootPort = NoPort;
    	
    	bridgeInfo.maxAge = bridgeInfo.bridgeMaxAge;
    	bridgeInfo.helloTime = bridgeInfo.bridgeHelloTime;
    	bridgeInfo.forwardDelay = bridgeInfo.bridgeForwardDelay;
    	
    	bridgeInfo.topologyChangeDetected = false;
    	bridgeInfo.topologyChange = false;
    	stopTCNTimer();
    	stopTopologyChangeTimer();
    	
    	for (int portNo = One; portNo <= NoOfPorts; portNo++) {
    	      initializePort(portNo);
    	   }
    	   portStateSelection();
    	   configBPDUGeneration();
    	   startHelloTimer();
    }
    
    private void initializePort(int portNo) {
    	becomeDesignatedPort(portNo);
    	
    	setPortState(portNo, State.Blocking);
    	
    	portInfo[portNo].topologyChangeAcknowledge = false;
    	
    	portInfo[portNo].configPending = false;
    	
    	portInfo[portNo].changeDetectionEnabled = true;
    	
    	stopMessageAgeTimer(portNo);
    	
    	stopForwardDelayTimer(portNo);
    	
    	stopHoldTimer(portNo);
    }
    
    public void enablePort(int portNo) {
    	initializePort(portNo);
    	
    	portStateSelection();
    }
    
    public void disablePort(int portNo) {
    	boolean root = rootBridge();
    	
    	becomeDesignatedPort(portNo);
    	
    	setPortState(portNo, State.Disabled);
    	
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
    	
    	for (int portNo = One; portNo <= NoOfPorts; portNo++) {
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
    	
    	for (int portNo = One; portNo <= NoOfPorts; portNo++) {
    		if (messageAgeTimerExpired(portNo)) {
    			messageAgeTimerExpiry(portNo);
    		}
    	}
    	for (int portNo = One; portNo <= NoOfPorts; portNo++) {
    		if (forwardDelayTimerExpired(portNo)) {
    			forwardDelayTimerExpiry(portNo);
    		}
    		if (holdTimerExpired(portNo)) {
    			holdTimerExpiry(portNo);
    		}
    	}
    }
    
    private void startHelloTimer() {
    	helloTimer.value = Zero;
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
    	tcnTimer.value = Zero;
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
    	topologyChangeTimer.value = Zero;
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
    	forwardDelayTimer[portNo].value = Zero;
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
    	holdTimer[portNo].value = Zero;
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
	long 			designatedCost = 0;
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
