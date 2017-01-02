package network.protocol.L2;

import java.util.Timer;
import java.util.TimerTask;

import network.datagram.L2.Frame;
import network.datagram.L2.STPFrame;
import network.device.Device;

public class STP extends Thread {
	private STPState state = null;
	private Device delegate = null;
	private int helloTime = 2;
	private int maxAge = 20;
	private int forwardDelay = 15;
	private int bridgePriority = 32779;
	
	public STP() {
		
	}
	
	public STP(Device delegate) {
		this.delegate = delegate;
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
		STPFrame frame = new STPFrame();
		frame.setHelloTime(this.helloTime);
		frame.setBridgeAddress(delegate.getMACAddress());
		frame.setRootId(frame.getBridgeId());
		System.out.println(frame.description());
	}
	
	public void run() {
		setState(new STPBlockingState());
		Timer timer = new Timer();
		timer.schedule(new SwitchTask(this), maxAge * 1000);
		timer.schedule(new SendHelloTask(this), this.helloTime * 1000, this.helloTime * 1000);
	}
	
	public void setState(STPState state) {
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
