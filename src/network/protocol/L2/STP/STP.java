package network.protocol.L2.STP;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
//import java.util.Timer;
import java.util.TimerTask;

import network.datagram.L2.Frame;
import network.datagram.L2.STPFrame;
import network.datagram.L2.Util;
import network.device.Device;
import network.device.Port;
import network.protocol.L2.STP.STP.State;

public class STP extends Thread {
	public enum State {
        Disabled, Listening, Learning, Forwarding, Blocking
	}
	
	final static int ConfigBPDUType = 0;
	final static int TCNBPDUType = 128;
	private Map<Integer, STPPortState> states = null;	
	private Device delegate = null;

	private int bridgePriority = 32779;
	private byte[] bridgeAddress;
	
    /* From IEEE Standard 802.1D 1998 Edition */
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
	
    private Map<Port, BPDU> configurationBPDUs = null;
    
        
    public STP() {
		
	}
	
	public STP(Device delegate) {
		this.delegate = delegate;
		
		/* Initialize port state as "disabled" for each port of device */
		states = new HashMap<Integer, STPPortState>();
		for (Port port: delegate.getPorts()) {
			states.put(port.hashCode(), new STPPortStateDisabled());
		}
		bridgeIdentifier = new byte[8];
		bridgePriority = 32779;
		bridgeAddress = delegate.getMACAddress();
		bridgeIdentifier[0] = (byte)(bridgePriority >> 8 & 0xFF);  
		bridgeIdentifier[1] = (byte)(bridgePriority & 0xFF);
		for (int i=0;i<6;i++) {
			bridgeIdentifier[i+2] = bridgeAddress[i];
		}
		designatedRoot = new byte[8];
		System.arraycopy(bridgeIdentifier, 0, designatedRoot, 0, 8);
	}
	
	public int getHelloTime() {
		return helloTime;
	}

	public void setHelloTime(int helloTime) {
		this.helloTime = helloTime;
	}

	public int getMaxAge() {
		return maxAge;
	}

	public void setMaxAge(int maxAge) {
		this.maxAge = maxAge;
	}

	public int getForwardDelay() {
		return forwardDelay;
	}

	public void setForwardDelay(int forwardDelay) {
		this.forwardDelay = forwardDelay;
	}
	
	public void sendHello() {
		if (!Arrays.equals(designatedRoot, bridgeIdentifier)) return;
		for (Port port: delegate.getPorts()) {
			if (!states.get(port.hashCode()).willSendBPDU()) continue;
			STPFrame stpFrame = new STPFrame();
			stpFrame.setHelloTime(this.helloTime);
			stpFrame.setBridgePriority(this.bridgePriority);
			stpFrame.setBridgeAddress(this.bridgeAddress);
			stpFrame.setRootId(this.designatedRoot);
			//stpFrame.setRootId(stpFrame.getBridgeId());
			//System.out.println(frame.description());
			//for (Port port: delegate.getPorts()) {
				stpFrame.setPathCost(this.rootPathCost + 2);
				Frame frame = new Frame();
				frame.setData(stpFrame.getBytes());
				frame.setDestination("FF:FF:FF:FF:FF:FF");
				frame.setSource(delegate.getMACAddress());
				delegate.sendFrame(frame);
			//}
		}
	}
	
	public void fetchFrame(Frame frame, int number) {
		STPFrame stpFrame = new STPFrame(frame);
		System.out.println(stpFrame.description());
		if(Util.isSmallAddr(stpFrame.getRootId(), this.designatedRoot, 8)) {
			this.designatedRoot = stpFrame.getRootId();
			this.rootPathCost = stpFrame.getPathCost();
			
			this.rootPort = number;
		}
	}
	
	public void run() {
		setState(new STPPortStateBlocking());
		Timer timer = new Timer();
		timer.schedule(new SwitchTask(this), maxAge * 1000);
		timer.schedule(new SendHelloTask(this), this.helloTime * 1000, this.helloTime * 1000);
	}
	
	public void setState(STPPortState state) {
		if (this.state != null) {
			System.out.println("state change " + this.state.getStateName() + "->" + state.getStateName());
		} else {
			System.out.println("state change ->" + state.getStateName());
		}
		this.state = state;
	}
	
    public void read(Frame frame) {
        state.actionForUserFrame();
    }
    
    /* From IEEE Standard 802.1D 1998 Edition */
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
    	return Arrays.equals(bridgeInfo.designatedRoot, bridgeInfo.bridgeId);
    }
    
    boolean supersedesPortInfo(int portNo, BPDU config) {
    	return (Util.isSmallAddr(config.rootId, portInfo[portNo].designatedRoot, 8) ||
    			(Arrays.equals(config.rootId, portInfo[portNo].designatedRoot) &&
    			(config.rootPathCost < portInfo[portNo].designatedCost ||
    			(config.rootPathCost == portInfo[portNo].designatedCost &&
    			(Util.isSmallAddr(config.bridgeId, portInfo[portNo].designatedBridge, 8) ||
    				(Arrays.equals(config.bridgeId, portInfo[portNo].designatedBridge) &&
    				(!Arrays.equals(config.bridgeId, bridgeInfo.bridgeId) || config.portId <= portInfo[portNo].designatedPort)    					 )
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
    	for (int portNo = 0; portNo < 8; portNo++) {
    		if (designatedPort(portNo) && portInfo[portNo].state != State.Disabled) {
    			transmitConfig(portNo);
    		}
    	}
    }
    
    private boolean designatedPort(int portNo) {
    	return Arrays.equals(portInfo[portNo].designatedBridge, bridgeInfo.bridgeId) &&
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
    				Util.isSmallAddr(portInfo[portNo].designatedRoot, bridgeInfo.bridgeId, 8) ) &&
    				(rootPort == NoPort || Util.isSmallAddr(portInfo[portNo].designatedRoot, portInfo[rootPort].designatedRoot, 8)) ||
    				( (portInfo[portNo].designatedRoot == portInfo[rootPort].designatedRoot) && 
    				(((portInfo[portNo].designatedCost + portInfo[portNo].pathCost) < (portInfo[rootPort].designatedCost + portInfo[rootPort].pathCost)) ||
    						(((portInfo[portNo].designatedCost + portInfo[portNo].pathCost) == (portInfo[rootPort].designatedCost + portInfo[rootPort].pathCost))
    								&&
    								(Util.isSmallAddr(portInfo[portNo].designatedBridge, portInfo[rootPort].designatedBridge, 8) || 
    										(Arrays.equals(portInfo[portNo].designatedBridge, portInfo[rootPort].designatedBridge)
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
    		if (designatedPort(portNo) || !Arrays.equals(portInfo[portNo].designatedRoot, bridgeInfo.designatedRoot) ||
    				bridgeInfo.rootPathCost < portInfo[portNo].designatedCost ||
    				((bridgeInfo.rootPathCost == portInfo[portNo].designatedCost) && (
    						Util.isSmallAddr(bridgeInfo.bridgeId, portInfo[portNo].designatedBridge, 8) ||
    						( (Arrays.equals(bridgeInfo.bridgeId, portInfo[portNo].designatedBridge) && 
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
    		stopForwardingDelayTimer(portNo);
    	}
    }
    
    private void setPortState(int portNo, State state) {
    	portInfo[portNo].state = state;
    }
	
    private void topologyChangeDetection() {
    	if (rootBridge())
    }
    
    private class SwitchTask extends TimerTask {
    	private STP delegate;
    	
    	SwitchTask(STP delegate) {
    		this.delegate = delegate;
    	}
    	
		public void run() { 
			delegate.setState(delegate.state.getNextState());
		}
	}
    
    private class SendHelloTask extends TimerTask {
    	private STP delegate;
    	
    	SendHelloTask(STP delegate) {
    		this.delegate = delegate;
    	}

		@Override
		public void run() {
			delegate.sendHello();
		}
    	
    }
}

class BridgeData {
	int type;
    byte[] designatedRoot = new byte[8];
    long rootPathCost;    /* Path Cost */
    int rootPort;
    int maxAge;       /* Max Age */
    int helloTime;    /* Hello Time */
    int forwardDelay; /* Forward Delay */
    byte[] bridgeId = new byte[8];
    int bridgeMaxAge;
    int bridgeHelloTime;
    int bridgeForwardDelay;
    boolean topologyChangeDetected;
    boolean topologyChange;
    int topologyChangeTime;
    int holdTime;
}

class PortData {
	int 			portId;
	State 	state;
	int 			pathCost;
	byte[] 			designatedRoot = new byte[8];
	long 			designatedCost = 0;
	byte[] 			designatedBridge = new byte[8];
	int 			designatedPort;
	boolean			topologyChangeAcknowledge;
	boolean			configPending;
	boolean			changeDetectionEnabled;
}

class Timer {
	boolean active;
	int value;
}
