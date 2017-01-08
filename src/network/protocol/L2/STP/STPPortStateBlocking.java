package network.protocol.L2.STP;

public class STPPortStateBlocking implements STPPortState {
	final static String stateName = "Blocking";
	final static STPPortState nextState = new STPPortStateListening(); 	
	
	@Override
	public String getStateName() {
		// TODO Auto-generated method stub
		return stateName;
	}
	
	@Override
	public void actionForUserFrame() {
		/* Discard recieved frame */
		return;
	}
	
	@Override
	public boolean willSendBPDU() {
		return false;
	}

	@Override
	public void learnMacFrame() {
		return;
	}

	@Override
	public STPPortState getNextState() {
		// TODO Auto-generated method stub
		return nextState;
	}
}
